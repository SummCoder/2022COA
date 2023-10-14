package cpu.alu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import static org.junit.Assert.assertEquals;

public class ALUMulTest {

	private final ALU alu = new ALU();
	private DataType src;
	private DataType dest;
	private DataType result;

	@Test
	public void MulTest1() {
		src = new DataType("00000000000000000000000000001010");
		dest = new DataType("00000000000000000000000000001010");
//		src = new DataType("11111111111111111111111111111001");
//		dest = new DataType("11111111111111111111111111111010");
		result = alu.mul(src, dest);
		assertEquals("00000000000000000000000001100100", result.toString());
	}

}
