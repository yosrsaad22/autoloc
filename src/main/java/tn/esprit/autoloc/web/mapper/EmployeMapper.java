package tn.esprit.autoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.web.dto.EmployeDTO;

@Mapper(componentModel = "spring")
public interface EmployeMapper {

    @Mapping(source = "idEmploye", target = "id")
    @Mapping(source = "agence.idAgence", target = "agenceId")
    EmployeDTO toDto(Employe employe);

    @Mapping(source = "id", target = "idEmploye")
    @Mapping(target = "agence", ignore = true)
    Employe toEntity(EmployeDTO dto);
}
