package com.ilmister.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ilmister.dto.ErroreCampo;
import com.ilmister.dto.ErroreResponse;

/**
 * Trasforma ogni eccezione in una risposta JSON uguale per tutti gli errori. Estende il gestore di Spring, che
 * già conosce gli errori standard (corpo illeggibile, parametro mancante, metodo non consentito...): qui si
 * cambia soltanto la forma della risposta.
 */
@RestControllerAdvice
public class GestoreEccezioni extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GestoreEccezioni.class);

    @ExceptionHandler(RisorsaNonTrovataException.class)
    ResponseEntity<Object> nonTrovata(RisorsaNonTrovataException e, WebRequest richiesta) {
        return risposta(HttpStatus.NOT_FOUND, "NON_TROVATO", e.getMessage(), richiesta);
    }

    @ExceptionHandler(OperazioneNonValidaException.class)
    ResponseEntity<Object> operazioneNonValida(OperazioneNonValidaException e, WebRequest richiesta) {
        return risposta(HttpStatus.BAD_REQUEST, "OPERAZIONE_NON_VALIDA", e.getMessage(), richiesta);
    }

    @ExceptionHandler(ConflittoException.class)
    ResponseEntity<Object> conflitto(ConflittoException e, WebRequest richiesta) {
        return risposta(HttpStatus.CONFLICT, "CONFLITTO", e.getMessage(), richiesta);
    }

    @ExceptionHandler(CredenzialiNonValideException.class)
    ResponseEntity<Object> nonAutenticato(CredenzialiNonValideException e, WebRequest richiesta) {
        return risposta(HttpStatus.UNAUTHORIZED, "NON_AUTENTICATO", e.getMessage(), richiesta);
    }

    @ExceptionHandler(AccountBloccatoException.class)
    ResponseEntity<Object> accountBloccato(AccountBloccatoException e, WebRequest richiesta) {
        return risposta(HttpStatus.FORBIDDEN, "ACCOUNT_BLOCCATO", e.getMessage(), richiesta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> accessoNegato(AccessDeniedException e, WebRequest richiesta) {
        return risposta(HttpStatus.FORBIDDEN, "ACCESSO_NEGATO", "Non hai i permessi per questa operazione", richiesta);
    }

    /** Due richieste insieme sulla stessa carriera (ad esempio un doppio clic su "Avanti"): vince la prima */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Object> concorrenza(OptimisticLockingFailureException e, WebRequest richiesta) {
        return risposta(HttpStatus.CONFLICT, "CONFLITTO_CONCORRENZA",
                "La carriera è stata modificata da un'altra richiesta: riprova", richiesta);
    }

    /** Un vincolo del database violato (ad esempio due registrazioni insieme con lo stesso username) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> vincolo(DataIntegrityViolationException e, WebRequest richiesta) {
        return risposta(HttpStatus.CONFLICT, "CONFLITTO", "I dati inviati entrano in conflitto con quelli esistenti",
                richiesta);
    }

    /** Qualunque altra cosa: si registra l'errore ma al cliente non si mostrano i dettagli interni */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> erroreGenerico(Exception e, WebRequest richiesta) {
        LOG.error("Errore non gestito", e);
        return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRORE_INTERNO", "Si è verificato un errore imprevisto",
                richiesta);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpHeaders intestazioni, HttpStatusCode stato, WebRequest richiesta) {
        List<ErroreCampo> campi = e.getBindingResult().getFieldErrors().stream()
                .map(errore -> new ErroreCampo(errore.getField(), errore.getDefaultMessage()))
                .toList();
        ErroreResponse corpo = new ErroreResponse(java.time.Instant.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDAZIONE", "Dati non validi", percorso(richiesta), campi);
        return ResponseEntity.badRequest().body(corpo);
    }

    /** Tutti gli altri errori standard di Spring MVC escono con la stessa forma */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object corpo, HttpHeaders intestazioni,
            HttpStatusCode stato, WebRequest richiesta) {
        HttpStatus http = HttpStatus.valueOf(stato.value());
        String messaggio = switch (http) {
            case BAD_REQUEST -> "Richiesta non valida";
            case NOT_FOUND -> "Risorsa non trovata";
            case METHOD_NOT_ALLOWED -> "Metodo non consentito";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo di contenuto non supportato";
            default -> http.getReasonPhrase();
        };
        return ResponseEntity.status(stato).headers(intestazioni)
                .body(ErroreResponse.di(stato.value(), http.name(), messaggio, percorso(richiesta)));
    }

    private ResponseEntity<Object> risposta(HttpStatus stato, String errore, String messaggio, WebRequest richiesta) {
        return ResponseEntity.status(stato).body(ErroreResponse.di(stato.value(), errore, messaggio, percorso(richiesta)));
    }

    private static String percorso(WebRequest richiesta) {
        return richiesta instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : "";
    }
}
