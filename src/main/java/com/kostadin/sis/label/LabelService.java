package com.kostadin.sis.label;

import com.kostadin.sis.common.exception.LabelBadRequestException;
import com.kostadin.sis.common.exception.LabelNotFoundException;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.label.model.request.SaveLabelCommand;
import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.mapper.LabelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper mapper;

    /**
     * Saving a Label to the DB. Method is used for creating SYSTEM scope labels only.
     * @param saveLabelCommand Request body containing only the label name
     * @return Maps saved Label to a {@link LabelDTO}
     */
    public LabelDTO saveLabel(SaveLabelCommand saveLabelCommand) {
        log.info("Saving new label {}",saveLabelCommand.getName());
        if (labelRepository.existsLabelWithName(saveLabelCommand.getName())){
            throw new LabelBadRequestException("SYSTEM scope label with name " + saveLabelCommand.getName() + " already exists.");
        }
        Label label = new Label()
                .setScope(LabelScope.SYSTEM)
                .setName(saveLabelCommand.getName());
        return mapper.toDto(labelRepository.save(label));
    }

    /**
     * Receives all labels from the database, filtered by Label scope.
     * @param labelScope Request parameter which labels are filtered by.
     * @return Maps returned list to {@link LabelDTO}
     */
    public List<LabelDTO> getLabels(LabelScope labelScope) {
        log.info("Receiving a list of all labels from the DB.");

        List<Label> labels = labelRepository.findAllWithScope(labelScope.name());
        if (labels.isEmpty()){
            log.debug("Received an empty list of labels.");
        }
        return mapper.toDto(labels);
    }

    /**
     * Receives a label from the DB by name.
     * @param id Label ID
     * @returns {@link LabelDTO}, never null
     * @throws CustomResponseStatusException, if Label with given ID is not found
     */
    public LabelDTO getLabel(long id) {
        log.info("Receiving label with ID {} from the DB.",id);

        return labelRepository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(() ->  new LabelNotFoundException("Label with ID " + id + " not found."));

    }

    /**
     * Updates an existing Label.
     * Throws exception if not such label is found.
     * @param id ID of label that is up to be updated
     * @param saveLabelCommand Body with updated fields.
     * @return Returns updated Label mapped to {@link LabelDTO}
     */
    public LabelDTO updateLabel(long id, SaveLabelCommand saveLabelCommand) {
        log.info("Updating label with ID {}",id);

        var label = labelRepository
                .findById(id)
                .orElseThrow(() -> new LabelNotFoundException("Label " + id + " not found."));

        label
                .setName(saveLabelCommand.getName());

        return mapper.toDto(labelRepository.save(label));
    }

    /**
     * Deletes a label from the DB.
     * Throws an exception if label with such name is not found.
     * @param id  of Label that is up to be deleted
     */
    public void deleteLabel(long id) {
        log.info("Deleting label with ID {}",id);
        var label = labelRepository
                .findById(id)
                .orElseThrow(() -> new LabelNotFoundException("Label " + id + " not found."));

        labelRepository.delete(label);
    }

    /**
     * Method used for autocompleting label names, filtered by their scope.
     * @param labelScope Label scope, which the query uses to filter result.
     * @param filter Label filter, used for filtering names that match it.
     * @return Mapped query result to a list of {@link LabelDTO}
     */
    public List<LabelDTO> findLabelsByScopeAndName(LabelScope labelScope, String filter) {
        return labelRepository.findAllByScopeAndName(labelScope.name(), filter)
                .stream().map(mapper::toDto)
                .toList();
    }
}
