package org.remitt.parser.x12dto;

import java.util.ArrayList;
import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class FunctionalAcknowledgement implements X12DTO {

	@Element(name = "transactionSetControlNumber")
	private String transactionSetControlNumber;

	@Element(name = "groupControlNumber")
	private String groupControlNumber;

	@ElementList(name = "transactionSets", required = false)
	private List<TransactionSet> transactionSets = new ArrayList<TransactionSet>();

	public FunctionalAcknowledgement() {
	}

	public FunctionalAcknowledgement(List<Segment> in) {
		processSegmentList(in);
	}
	
	@Override
	public void processSegmentList(List<Segment> in) {
		Segment ST = X12Message.findSegmentByComparator(in,
				new SegmentComparator("ST"));
		this.setTransactionSetControlNumber(X12Message.getSafeElement(ST, 2));

		Segment AK1 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("AK1"));
		this.setGroupControlNumber(X12Message.getSafeElement(AK1, 2));
	}

	public String getTransactionSetControlNumber() {
		return transactionSetControlNumber;
	}

	public void setTransactionSetControlNumber(
			String transactionSetControlNumber) {
		this.transactionSetControlNumber = transactionSetControlNumber;
	}

	public String getGroupControlNumber() {
		return groupControlNumber;
	}

	public void setGroupControlNumber(String groupControlNumber) {
		this.groupControlNumber = groupControlNumber;
	}

	public List<TransactionSet> getTransactionSets() {
		return transactionSets;
	}

	public void setTransactionSets(List<TransactionSet> transactionSets) {
		this.transactionSets = transactionSets;
	}

	public void addTransactionSet(TransactionSet transactionSet) {
		this.transactionSets.add(transactionSet);
	}
	
	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
