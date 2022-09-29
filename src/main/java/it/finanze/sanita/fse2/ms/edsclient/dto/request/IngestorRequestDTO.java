package it.finanze.sanita.fse2.ms.edsclient.dto.request;

import it.finanze.sanita.fse2.ms.edsclient.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngestorRequestDTO {

    @Nullable
    private EdsMetadataUpdateReqDTO updateReqDTO;

    @Nullable
    private IniEdsInvocationETY iniEdsInvocationETY;

    @Nullable
    private PriorityTypeEnum priorityType;

    private ProcessorOperationEnum operation;

    private String identifier;

}
