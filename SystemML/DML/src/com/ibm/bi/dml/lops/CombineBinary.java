package com.ibm.bi.dml.lops;

import java.util.HashSet;

import com.ibm.bi.dml.lops.LopProperties.ExecLocation;
import com.ibm.bi.dml.lops.LopProperties.ExecType;
import com.ibm.bi.dml.lops.compile.JobType;
import com.ibm.bi.dml.parser.Expression.DataType;
import com.ibm.bi.dml.parser.Expression.ValueType;
import com.ibm.bi.dml.utils.LopsException;


/**
 * Lop to represent an combine operation -- used ONLY in the context of sort.
 */

public class CombineBinary extends Lops 
{
	
	public enum OperationTypes {PreSort, PreCentralMoment, PreCovUnweighted, PreGroupedAggUnweighted}; // (PreCovWeighted,PreGroupedAggWeighted) will be CombineTertiary	
	OperationTypes operation;

	/**
	 * @param input - input lop
	 * @param op - operation type
	 */
	
	public CombineBinary(OperationTypes op, Lops input1, Lops input2, DataType dt, ValueType vt) 
	{
		super(Lops.Type.CombineBinary, dt, vt);	
		operation = op;
		this.addInput(input1);
		this.addInput(input2);
		input1.addOutput(this);
		input2.addOutput(this);
		
		/*
		 *  This lop can ONLY be executed as a STANDALONE job
		 */
		boolean breaksAlignment = false;
		boolean aligner = false;
		boolean definesMRJob = true;
		lps.addCompatibility(JobType.COMBINE);
		this.lps.setProperties( ExecType.MR, ExecLocation.MapAndReduce, breaksAlignment, aligner, definesMRJob );
	}
	
	/**
	 * for debugging purposes. 
	 */
	
	public String toString()
	{
		return "combinebinary";		
	}

	@Override
	public String getInstructions(int input_index1, int input_index2, int output_index) 
		throws LopsException
	{
		// Determine whether or not the second input denotes weights vector.
		// CombineBinary can be used to combine (data,weights) vectors or (data1,data2) vectors  
		boolean isSecondInputIsWeight = true;
		if ( operation == OperationTypes.PreCovUnweighted || operation == OperationTypes.PreGroupedAggUnweighted ) {
			isSecondInputIsWeight = false;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append( getExecType() );
		sb.append( Lops.OPERAND_DELIMITOR );
		sb.append( "combinebinary" );
		sb.append( OPERAND_DELIMITOR );
		sb.append( isSecondInputIsWeight );
		sb.append( DATATYPE_PREFIX );
		sb.append( DataType.SCALAR );
		sb.append( VALUETYPE_PREFIX );
		sb.append( ValueType.BOOLEAN );
		sb.append( OPERAND_DELIMITOR );
		sb.append( input_index1 );
		sb.append( DATATYPE_PREFIX );
		sb.append( getInputs().get(0).get_dataType() );
		sb.append( VALUETYPE_PREFIX );
		sb.append( getInputs().get(0).get_valueType() );
		sb.append( OPERAND_DELIMITOR );
		sb.append( input_index2 );
		sb.append( DATATYPE_PREFIX );
		sb.append( getInputs().get(1).get_dataType() );
		sb.append( VALUETYPE_PREFIX );
		sb.append( getInputs().get(1).get_valueType() );
		sb.append( OPERAND_DELIMITOR );
		sb.append( output_index );
		sb.append( DATATYPE_PREFIX );
		sb.append( get_dataType() );
		sb.append( VALUETYPE_PREFIX );
		sb.append( get_valueType() );
		
		return sb.toString();
	}

	public OperationTypes getOperation() { 
		return operation;
	}
	
	public static CombineBinary constructCombineLop(OperationTypes op, Lops input1, 
			Lops input2, DataType dt, ValueType vt) {
		
		HashSet<Lops> set1 = new HashSet<Lops>();
		set1.addAll(input1.getOutputs());
		
		// find intersection of input1.getOutputs() and input2.getOutputs();
		set1.retainAll(input2.getOutputs());
		
		for (Lops lop  : set1) {
			if ( lop.type == Lops.Type.CombineBinary ) {
				CombineBinary combine = (CombineBinary)lop;
				if ( combine.operation == op)
					return (CombineBinary)lop;
			}
		}
		
		CombineBinary comn = new CombineBinary(op, input1, input2, dt, vt);
		comn.setAllPositions(input1.getBeginLine(), input1.getBeginColumn(), input1.getEndLine(), input1.getEndColumn());
		return comn;
	}
 
}
