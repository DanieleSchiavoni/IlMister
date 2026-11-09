package com.ilmister.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.RigaClassificaResponse;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.VoceClassifica;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.ClassificaRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.service.ClassificaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassificaServiceImpl implements ClassificaService {

    private final CarrieraRepository carrieraRepository;
    private final StagioneRepository stagioneRepository;
    private final SquadraRepository squadraRepository;
    private final ClassificaRepository classificaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RigaClassificaResponse> classifica(Long carrieraId, Long utenteId) {
        carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        // La stagione più recente: quella in corso o, a fine campionato, l'ultima conclusa
        Stagione stagione = stagioneRepository.findByCarrieraIdOrderByAnnoInizioDesc(carrieraId).stream()
                .findFirst()
                .orElseThrow(() -> new RisorsaNonTrovataException("Stagione non trovata"));
        Squadra dellUtente = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));

        List<VoceClassifica> voci = classificaRepository.classifica(stagione.getId(),
                dellUtente.getCampionato().getId());
        List<RigaClassificaResponse> righe = new ArrayList<>();
        for (int i = 0; i < voci.size(); i++) {
            VoceClassifica voce = voci.get(i);
            Squadra squadra = voce.getSquadra();
            righe.add(new RigaClassificaResponse(i + 1, squadra.getId(), squadra.getNome(), voce.getGiocate(),
                    voce.getVittorie(), voce.getPareggi(), voce.getSconfitte(), voce.getGolFatti(),
                    voce.getGolSubiti(), voce.differenzaReti(), voce.getPunti(), squadra.getId().equals(dellUtente.getId())));
        }
        return righe;
    }
}
