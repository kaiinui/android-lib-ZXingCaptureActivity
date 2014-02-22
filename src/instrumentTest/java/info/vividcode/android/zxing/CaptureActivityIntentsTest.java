package info.vividcode.android.zxing;

import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class CaptureActivityIntentsTest extends InstrumentationTestCase {

  public static void test_setDecodeFormatsToIntent() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeFormatsToIntent(intent,
          Arrays.asList(BarcodeFormat.CODE_39, BarcodeFormat.AZTEC, BarcodeFormat.CODABAR));
      MoreAsserts.assertContentsInAnyOrder(DecodeFormatManager.parseDecodeFormats(intent),
          BarcodeFormat.CODE_39, BarcodeFormat.AZTEC, BarcodeFormat.CODABAR);
    }
    { // One item
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeFormatsToIntent(intent,
          Arrays.asList(BarcodeFormat.CODE_39));
      MoreAsserts.assertContentsInAnyOrder(DecodeFormatManager.parseDecodeFormats(intent),
          BarcodeFormat.CODE_39);
    }
    { // Empty list
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeFormatsToIntent(intent, Arrays.<BarcodeFormat>asList());
      assertNull("In case that empty formats list is passed, value is not set",
          DecodeFormatManager.parseDecodeFormats(intent));
    }
  }

  private static void doTestSetDecodeModeToIntent(
      String modeStr, Collection<BarcodeFormat> expectedFormats) {
    Intent intent = new Intent("DUMMY_ACTION");
    CaptureActivityIntents.setDecodeModeToIntent(intent, modeStr);
    Collection<BarcodeFormat> decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
    MoreAsserts.assertContentsInAnyOrder(decodeFormats, expectedFormats.toArray());
  }

  public static void test_setDecodeModeToIntent() {
    {
      doTestSetDecodeModeToIntent(Intents.Scan.PRODUCT_MODE, DecodeFormatManager.PRODUCT_FORMATS);
      doTestSetDecodeModeToIntent(Intents.Scan.QR_CODE_MODE, DecodeFormatManager.QR_CODE_FORMATS);
      doTestSetDecodeModeToIntent(Intents.Scan.ONE_D_MODE, DecodeFormatManager.ONE_D_FORMATS);
      doTestSetDecodeModeToIntent(
          Intents.Scan.DATA_MATRIX_MODE, DecodeFormatManager.DATA_MATRIX_FORMATS);
    }
    { // Invalid mode string
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeModeToIntent(intent, "");
      Collection<BarcodeFormat> decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
      assertNull(decodeFormats);
    }
  }

  public static void test_setDecodeHintToIntentMethods() {
    class TestProcess {
      void assertThatSpecifiedHintTypeEnabled(Intent intent, DecodeHintType type) {
        Map<DecodeHintType, Object> hintMap = DecodeHintManager.parseDecodeHints(intent);
        assertEquals(1, hintMap.size());
        assertTrue(hintMap.containsKey(type));
        Object value = hintMap.get(type);
        MoreAsserts.assertAssignableFrom(Boolean.class, value.getClass());
        assertEquals(Boolean.TRUE, value);
      }
    }
    TestProcess p = new TestProcess();
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeHintPureBarcodeEnabledToIntent(intent);
      p.assertThatSpecifiedHintTypeEnabled(intent, DecodeHintType.PURE_BARCODE);
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeHintTryHarderEnabledToIntent(intent);
      p.assertThatSpecifiedHintTypeEnabled(intent, DecodeHintType.TRY_HARDER);
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeHintAssumeCode39CheckDigitEnabledToIntent(intent);
      p.assertThatSpecifiedHintTypeEnabled(intent, DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT);
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeHintAssumeGs1EnabledToIntent(intent);
      p.assertThatSpecifiedHintTypeEnabled(intent, DecodeHintType.ASSUME_GS1);
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setDecodeHintReturnCodabarStartEndEnabledToIntent(intent);
      p.assertThatSpecifiedHintTypeEnabled(intent, DecodeHintType.RETURN_CODABAR_START_END);
    }
  }

  public static void test_setDecodeHintAllowedLengthsToIntent() {
    class TestProcess {
      void exec(int[] lengths) {
        Intent intent = new Intent("DUMMY_ACTION");
        CaptureActivityIntents.setDecodeHintAllowedLengthsToIntent(intent, lengths);
        Map<DecodeHintType, Object> hintMap = DecodeHintManager.parseDecodeHints(intent);
        assertEquals(1, hintMap.size());
        assertTrue(hintMap.containsKey(DecodeHintType.ALLOWED_LENGTHS));
        Object value = hintMap.get(DecodeHintType.ALLOWED_LENGTHS);
        MoreAsserts.assertAssignableFrom(int[].class, value.getClass());
        MoreAsserts.assertEquals(lengths, (int[]) value);
      }
    }
    TestProcess p = new TestProcess();
    p.exec(new int[] { 1, 2, 3 });
    p.exec(new int[] {}); // empty array
  }

  public void test_setPromptMessageToIntent() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setPromptMessageToIntent(intent, "message");
      assertEquals("message", CaptureActivityIntents.getPromptMessageFromIntentOrNull(intent));
    }
  }

  public void test_getPromptMessageFromIntentOrNull() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setPromptMessageToIntent(intent, "message");
      assertEquals("message", CaptureActivityIntents.getPromptMessageFromIntentOrNull(intent));
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      assertNull(CaptureActivityIntents.getPromptMessageFromIntentOrNull(intent));
    }
    {
      assertNull(CaptureActivityIntents.getPromptMessageFromIntentOrNull(null));
    }
  }

  public void test_setSizeOfScanningRectangleInPxToIntent() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setSizeOfScanningRectangleInPxToIntent(intent, 5, 10);
      assertEquals(5, CaptureActivityIntents.getWidthOfScanningRectangleInPxFromIntentOrZero(intent));
      assertEquals(10, CaptureActivityIntents.getHeightOfScanningRectangleInPxFromIntentOrZero(intent));
    }
  }

  public void test_getWidthAndHeightOfScanningRectangleInPxToIntent() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      CaptureActivityIntents.setSizeOfScanningRectangleInPxToIntent(intent, 5, 10);
      assertEquals(5, CaptureActivityIntents.getWidthOfScanningRectangleInPxFromIntentOrZero(intent));
      assertEquals(10, CaptureActivityIntents.getHeightOfScanningRectangleInPxFromIntentOrZero(intent));
    }
    { // In case that size is not specified, it returns zero.
      Intent intent = new Intent("DUMMY_ACTION");
      assertEquals(0, CaptureActivityIntents.getWidthOfScanningRectangleInPxFromIntentOrZero(intent));
      assertEquals(0, CaptureActivityIntents.getHeightOfScanningRectangleInPxFromIntentOrZero(intent));
    }
    { // In case that argument is `null`, it returns zero.
      assertEquals(0, CaptureActivityIntents.getWidthOfScanningRectangleInPxFromIntentOrZero(null));
      assertEquals(0, CaptureActivityIntents.getHeightOfScanningRectangleInPxFromIntentOrZero(null));
    }
  }

  public void test_setResultDisplayDurationInMsFromIntent() {
    {
      Intent intent = new Intent("DUMMY_ACTION");
      long setValue = 300L;
      try {
        CaptureActivityIntents.setResultDisplayDurationInMsToIntent(intent, setValue);
        assertTrue("Error not occurred", true);
      } catch (Throwable err) {
        assertTrue("Error occurred: " + err.getMessage(), false);
      }
    }
  }

  public void test_getResultDisplayDurationInMsFromIntent() {
    long defaultValue = 1500L;
    {
      Intent intent = new Intent("DUMMY_ACTION");
      long setValue = 300L;
      CaptureActivityIntents.setResultDisplayDurationInMsToIntent(intent, setValue);
      long val = CaptureActivityIntents.getResultDisplayDurationInMsFromIntentOrDefaultValue(intent);
      assertEquals("Returns set value", setValue, val);
    }
    {
      long val = CaptureActivityIntents.getResultDisplayDurationInMsFromIntentOrDefaultValue(null);
      assertEquals("Returns default value if argument is `null`", defaultValue, val);
    }
    {
      Intent intent = new Intent("DUMMY_ACTION");
      long val = CaptureActivityIntents.getResultDisplayDurationInMsFromIntentOrDefaultValue(intent);
      assertEquals("Returns default value if `intent` doesn't have display duration",
          defaultValue, val);
    }
  }

}