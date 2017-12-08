package basenet.better.basenet;

import org.junit.Test;

import lib.basenet.utils.FileUtils;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() throws Exception {
		assertEquals(4, 2 + 2);
	}

	@Test
	public void testMimeType() {
		System.out.println("aaa");
		System.out.println(FileUtils.getMimeType("http://storage.jd.com/tripitaka-jenkins/auto.zip"));
	}
}