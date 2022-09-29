package it.finanze.sanita.fse2.ms.edsclient.dto;



import it.finanze.sanita.fse2.ms.edsclient.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReferenceDTO {
	
	private String identifier;
	
	private ProcessorOperationEnum operation;
	
	private String jsonString;
	
	private PriorityTypeEnum priorityType;
}