package tn.esprit.autoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "idReservation", target = "id")
    @Mapping(source = "client.idClient", target = "clientId")
    @Mapping(target = "clientNomComplet",
            expression = "java(reservation.getClient() == null ? null : reservation.getClient().getPrenom() + \" \" + reservation.getClient().getNom())")
    @Mapping(source = "vehicule.idVehicule", target = "vehiculeId")
    @Mapping(source = "vehicule.immatriculation", target = "vehiculeImmatriculation")
    @Mapping(target = "tarifEstime", ignore = true)
    ReservationResponseDTO toDto(Reservation reservation);
}
