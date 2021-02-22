package iniregex;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Tests {

	@Test
	void testCorrectIni() {
		String[] correctini = new String[] {
				"; CORRECT REGEX",
				"",
				"[demosection]",
				"prop1=he llo/asd",
				"prop2=worl d/qwe",
				"foo=bar",
				"",
				"[infosection]",
				"filelength=20",
				"totalfiles=100"
			};
			
		// check if valid
		inivalidator correctinivalidator = new inivalidator(correctini);
		assertTrue(correctinivalidator.isValid());
		
		// check sections
		String[] sections = correctinivalidator.listSections();
		assertArrayEquals(sections, new String[] {"demosection", "infosection"});
		
		// print check keys in demosection
		String[] keys = correctinivalidator.listSectionKeys("demosection");
		assertArrayEquals(keys, new String[] {"prop1", "prop2", "foo"});
		
		// check demosection prop1
		assertEquals(correctinivalidator.getSectionKey("demosection", "prop1"), "he llo/asd");
	}
	
	@Test
	void testLongWrongIni() {
		String longstringwrongini = "\n"
			+ "; ONE STRING WRONG REGEX (comment on same line as property)\n"
			+ "[database]\n"
			+ "type=sql\n"
			+ "totalusers=253\n"
			+ "owner=root ;will change later";
		
		inivalidator longwronginivalidator = new inivalidator(longstringwrongini);
		assertFalse(longwronginivalidator.isValid());
		
		assertArrayEquals(longwronginivalidator.getErrorLines(), new int[] {5});
	}
	
	@Test
	void testMultipleWrongIni() {
		String[] multiplewrongini = new String[] {
			"; MULTIPLE ERRORS REGEX (section and property on same line)",
			"[serversetup]    port83",
			"hostname=google.com",
			"ip=192.168.3.235",
			"ping=pong",
			"",
			"command=help [permissions]",
			"owner=root",
			"moderator=alice,bob"
		};
		
		inivalidator multipleerrorini = new inivalidator(multiplewrongini);
		assertFalse(multipleerrorini.isValid());
		
		assertArrayEquals(multipleerrorini.getErrorLines(), new int[] {1, 6});
	}
	
	@Test
	void testCompressedIni() {
		String[] compressedini = new String[] {
			"; COMPRESSED INI (ini with least amount of empty lines)",
			"[github]",
			"owner=Microsoft",
			"branch=master",
			"forks=20",
			"[gitlab]",
			"type=opensource",
			"setupsteps=8"
		};
		
		inivalidator compressedinivalidator = new inivalidator(compressedini);
		assertTrue(compressedinivalidator.isValid());
		
		String[] sections = compressedinivalidator.listSections();
		assertArrayEquals(sections, new String[] {"github", "gitlab"});
		
		String[] keys = compressedinivalidator.listSectionKeys("gitlab");
		assertArrayEquals(keys, new String[] {"type", "setupsteps"});
		
		assertEquals(compressedinivalidator.getSectionKey("gitlab", "type"), "opensource");
	}
	
	@Test
	void testWindowsIni() {
		String[] windowsini = new String[] {
			"; WINDOWS INI (ini from windows)",
			"[boot loader]",
			"timeout=30",
			"default=multi(0)disk(0)rdisk(0)partition(1)WINDOWS",
			"[operating systems]",
			"edition=Microsoft Windows XP Professional"
		};
			
		inivalidator wininivalidator = new inivalidator(windowsini);
		assertTrue(wininivalidator.isValid());
		
		String[] sections = wininivalidator.listSections();
		assertArrayEquals(sections, new String[] {"boot loader", "operating systems"});
		
		String[] keys = wininivalidator.listSectionKeys("operating systems");
		assertArrayEquals(keys, new String[] {"edition"});
		
		assertEquals(wininivalidator.getSectionKey("operating systems", "edition"), "Microsoft Windows XP Professional");
	}
	
	@Test
	void testAllErrorsIni() {
		String[] allerrorsini = new String[] {
				"[starthere] ; OOPS ALL ERRORS",
				"browser=chrome ;could be firefox or edge",
				"country=us ; country might change ??",
				"[programming languages] best=java ; debatable",
				"java=script   ; not the same",
				"machine learning=python [end]"
		};
		
		inivalidator allerrorsvalidator = new inivalidator(allerrorsini);
		
		assertFalse(allerrorsvalidator.isValid());
		
		assertArrayEquals(allerrorsvalidator.getErrorLines(), new int[] {0, 1, 2, 3, 4, 5});
	}
	
	@Test
	void testOnlyNumbers() {
		String[] onlynumbers = new String[] {
			"; only numbers from here on out",
			"[11421]",
			"0=123",
			"1=456",
			"2=789",
			"10=123000"
		};
		
		inivalidator onlynumbersvalidator = new inivalidator(onlynumbers);
		
		assertTrue(onlynumbersvalidator.isValid());
		
		assertArrayEquals(onlynumbersvalidator.listSections(), new String[] {"11421"});
		
		assertArrayEquals(onlynumbersvalidator.listSectionKeys("11421"), new String[] {"0", "1", "2", "10"});
		
		assertEquals(onlynumbersvalidator.getSectionKey("11421", "0"), "123");
		
		assertEquals(onlynumbersvalidator.getSectionKey("11421", "10"), "123000");
	}
	
	@Test
	void testOnlySpecialCharacters() {
		String[] onlyspecialchars = new String[] {
			"; only special from here on out",
			"[!#$]",
			"(&)=!@#",
			"[[[!!!]]]=###",
			"; @#(*$&@#(%(@#*(@#^@$"
		};
		
		inivalidator specialcharvalidator = new inivalidator(onlyspecialchars);
		
		assertTrue(specialcharvalidator.isValid());
		
		assertArrayEquals(specialcharvalidator.listSections(), new String[] {"!#$"});
		
		assertArrayEquals(specialcharvalidator.listSectionKeys("!#$"), new String[] {"(&)", "[[[!!!]]]"});
		
		assertEquals(specialcharvalidator.getSectionKey("!#$", "(&)"), "!@#");
		
		assertEquals(specialcharvalidator.getSectionKey("!#$", "[[[!!!]]]"), "###");
	}
}
