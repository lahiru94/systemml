/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.runtime.instructions.cp;

import org.apache.sysml.lops.SortKeys;
import org.apache.sysml.parser.Expression.DataType;
import org.apache.sysml.parser.Expression.ValueType;
import org.apache.sysml.runtime.DMLRuntimeException;
import org.apache.sysml.runtime.controlprogram.context.ExecutionContext;
import org.apache.sysml.runtime.instructions.InstructionUtils;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.runtime.matrix.operators.Operator;
import org.apache.sysml.runtime.matrix.operators.SimpleOperator;

/**
 * This class supports two variants of sort operation on a 1-dimensional input matrix. 
 * The two variants are <code> weighted </code> and <code> unweighted </code>.
 * Example instructions: 
 *     sort:mVar1:mVar2 (input=mVar1, output=mVar2)
 *     sort:mVar1:mVar2:mVar3 (input=mVar1, weights=mVar2, output=mVar3)
 *  
 */
public class QuantileSortCPInstruction extends UnaryCPInstruction {

	private QuantileSortCPInstruction(Operator op, CPOperand in, CPOperand out, String opcode, String istr) {
		this(op, in, null, out, opcode, istr);
	}

	private QuantileSortCPInstruction(Operator op, CPOperand in1, CPOperand in2, CPOperand out, String opcode,
			String istr) {
		super(CPType.QSort, op, in1, in2, out, opcode, istr);
	}

	public static QuantileSortCPInstruction parseInstruction ( String str ) 
		throws DMLRuntimeException 
	{
		CPOperand in1 = new CPOperand("", ValueType.UNKNOWN, DataType.UNKNOWN);
		CPOperand in2 = null;
		CPOperand out = new CPOperand("", ValueType.UNKNOWN, DataType.UNKNOWN);
		
		String[] parts = InstructionUtils.getInstructionPartsWithValueType(str);
		String opcode = parts[0];
		
		if ( opcode.equalsIgnoreCase(SortKeys.OPCODE) ) {
			if ( parts.length == 3 ) {
				// Example: sort:mVar1:mVar2 (input=mVar1, output=mVar2)
				parseUnaryInstruction(str, in1, out);
				return new QuantileSortCPInstruction(new SimpleOperator(null), in1, out, opcode, str);
			}
			else if ( parts.length == 4 ) {
				// Example: sort:mVar1:mVar2:mVar3 (input=mVar1, weights=mVar2, output=mVar3)
				in2 = new CPOperand("", ValueType.UNKNOWN, DataType.UNKNOWN);
				parseUnaryInstruction(str, in1, in2, out);
				return new QuantileSortCPInstruction(new SimpleOperator(null), in1, in2, out, opcode, str);
			}
			else {
				throw new DMLRuntimeException("Invalid number of operands in instruction: " + str);
			}
		} 
		else {
			throw new DMLRuntimeException("Unknown opcode while parsing a QuantileSortCPInstruction: " + str);
		}
	}
	
	@Override
	public void processInstruction(ExecutionContext ec)
			throws DMLRuntimeException 
	{
		//acquire inputs matrices
		MatrixBlock matBlock = ec.getMatrixInput(input1.getName(), getExtendedOpcode());
		MatrixBlock wtBlock = null;
 		if (input2 != null) {
			wtBlock = ec.getMatrixInput(input2.getName(), getExtendedOpcode());
		}
		
 		//process core instruction
		MatrixBlock resultBlock = (MatrixBlock) matBlock.sortOperations(wtBlock, new MatrixBlock());
		
		//release inputs
		ec.releaseMatrixInput(input1.getName(), getExtendedOpcode());
		if (input2 != null)
			ec.releaseMatrixInput(input2.getName(), getExtendedOpcode());
		
		//set and release output
		ec.setMatrixOutput(output.getName(), resultBlock, getExtendedOpcode());
	}
}
