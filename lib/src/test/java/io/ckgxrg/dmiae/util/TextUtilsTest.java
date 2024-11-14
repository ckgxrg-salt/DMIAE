package io.ckgxrg.dmiae.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.exceptions.FormatException;
import java.util.HashSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Test static methods provided by TextUtils. */
public class TextUtilsTest {
  /** Begin testing. */
  @BeforeAll
  public static void test() {
    System.out.println("Begin testing class " + TextUtils.class.toString());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void rmSpaceTest() {
    String result = TextUtils.rmSpaces("    T his i s a Tes t");
    assertEquals("T his i s a Tes t", result);
  }

  @Test
  public void rmNameTest() {
    String result = TextUtils.rmName("Test: Hi, I'm a Test! ", "Test");
    assertEquals("Hi, I'm a Test!", result);
  }

  @Test
  public void untilLetterTest() {
    String result = TextUtils.untilLetter(" &*&@68shas*9sawj ");
    assertEquals("68shas*9sawj", result);
  }

  @Test
  public void rmColonTest() {
    String result1 = TextUtils.rmColon(">:Subline 1");
    assertEquals("Subline 1", result1);
    String result2 = TextUtils.rmColon("This: should not be touched.");
    assertEquals("This: should not be touched.", result2);
  }

  @Test
  public void identifyAnnoTypeTest() {
    try {
      AnnotationType result1 = TextUtils.identifyAnnoType("@:LIGHTING:Red 1 Blue 2");
      assertEquals(AnnotationType.LIGHTING, result1);
      AnnotationType result2 = TextUtils.identifyAnnoType("@:L:Red 1 Blue 2");
      assertEquals(AnnotationType.LIGHTING, result2);
      AnnotationType result3 = TextUtils.identifyAnnoType("@:MUSIC:Gaster's Theme");
      assertEquals(AnnotationType.AUDIO, result3);
      AnnotationType result4 = TextUtils.identifyAnnoType("@:NOTE:Testy!");
      assertEquals(AnnotationType.NOTE, result4);
      AnnotationType result5 = TextUtils.identifyAnnoType("@:Hohooh");
      assertEquals(AnnotationType.NOTE, result5);
    } catch (FormatException e) {
      Assertions.fail();
    }
  }

  @Test
  public void identifyAnnoTypeExceptionTest() {
    assertThrows(
        FormatException.class,
        () -> {
          TextUtils.identifyAnnoType("@This should fail");
        },
        "Wrong annotation format");
  }

  @Test
  public void getAnnoContentTest() {
    try {
      String result1 = TextUtils.getAnnoContent("@:LIGHTING:Red 1 Blue 2");
      assertEquals("Red 1 Blue 2", result1);
      String result2 = TextUtils.getAnnoContent("@:L:Red 1 Blue 2");
      assertEquals("Red 1 Blue 2", result2);
      String result3 = TextUtils.getAnnoContent("@:MUSIC:Gaster's Theme");
      assertEquals("Gaster's Theme", result3);
      String result4 = TextUtils.getAnnoContent("@:NOTE:Testy!");
      assertEquals("Testy!", result4);
      String result5 = TextUtils.getAnnoContent("@:Hohooh");
      assertEquals("Hohooh", result5);
    } catch (FormatException e) {
      Assertions.fail();
    }
  }

  @Test
  public void getAnnoContentExceptionTest() {
    assertThrows(
        FormatException.class,
        () -> {
          TextUtils.getAnnoContent("@This should fail");
        },
        "Wrong annotation format");
  }

  @Test
  public void getCharaNameTest() {
    Character chara =
        new Character("Chara", "chara", "The Devil that Comes any time when their Name is Called");
    Character frisk = new Character("Frisk", "The Fallen Human");
    HashSet<Character> charas = new HashSet<Character>();
    charas.add(chara);
    charas.add(frisk);
    String result = TextUtils.getCharaName(charas);
    if (result.equals("@Frisk / @Chara") || result.equals("@Chara / @Frisk")) {
      Assertions.assertTrue(true);
    } else {
      Assertions.fail();
    }
  }

  /** Finish testing. */
  @AfterAll
  public static void end() {
    System.out.println("Finished testing class " + TextUtils.class.toString());
  }
}
