package org.remitt.parser.x12dto;

import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Element;

public class TransactionSet implements X12DTO {

	@Element(name = "acknowledgementCode")
	private String acknowledgementCode;
	
	public TransactionSet() {
	}

	public TransactionSet(List<Segment> in) {
		processSegmentList(in);
	}
	
	@Override
	public void processSegmentList(List<Segment> in) {
		Segment AK2 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("AK2"));
		Segment AK5 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("AK5"));
		this.setAcknowledgementCode(X12Message.getSafeElement(AK5, 2));
	}
	
	public String getAcknowledgementCode() {
		return acknowledgementCode;
	}

	public void setAcknowledgementCode(String acknowledgementCode) {
		this.acknowledgementCode = acknowledgementCode;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
