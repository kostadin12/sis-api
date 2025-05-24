package com.kostadin.sis.mapper;

import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.response.LabelDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface LabelMapper {
    LabelDTO toDto(Label label);

    Label toEntity(LabelDTO labelDTO);

    List<LabelDTO> toDto(List<Label> labels);

    List<Label> toEntity(List<LabelDTO> labelDTOS);
}
