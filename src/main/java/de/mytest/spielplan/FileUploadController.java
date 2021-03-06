package de.mytest.spielplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class FileUploadController {

  private static final String ATTR_DOWNLOAD = "download";

  private static final String ATTR_MESSAGE = "message";

  private static final String ATTR_RESULT = "result";

  private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

  @RequestMapping(method = RequestMethod.GET)
  public String provideUploadInfo(final Model model) throws IOException {
    return "uploadForm";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String handleFileUpload(@RequestParam("file") final MultipartFile file,
      final RedirectAttributes redirectAttributes) {
    if (!file.isEmpty()) {
      BufferedReader input = null;
      try {
        input = new BufferedReader(
            new InputStreamReader(file.getInputStream(), "Cp1252"));
        final JsonReader jsonReader = Json.createReader(input);
        final JsonObject jsonObject = jsonReader.readObject();

        final String start = jsonObject.getString("start");
        final Date startDate = parseDate(start);

        final JsonArray jsonArray = jsonObject.getJsonArray("teams");
        final int size = jsonArray.size();
        final String[] teams = new String[size];
        for (int i = 0; i < jsonArray.size(); i++) {
          final JsonObject teamObject = jsonArray.getJsonObject(i);
          teams[i] = teamObject.getString("name");
        }

        final SpielPlan spielPlan = new SpielPlan(startDate, teams);
        final JsonArray result = spielPlan.plan();
        redirectAttributes.addFlashAttribute(ATTR_RESULT, result);
        redirectAttributes.addFlashAttribute(ATTR_DOWNLOAD,
            "Herunterladen als TXT-Datei");
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE,
            file.getOriginalFilename() + " erfolgreich planen!");
      } catch (final Exception e) {
        log.debug(e.getMessage());
        redirectAttributes.addFlashAttribute(ATTR_DOWNLOAD, "");
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE,
            "Fehler beim Planen " + file.getOriginalFilename() + " => " + e.getMessage());
      } finally {
        try {
          if (input != null) {
            input.close();
          }
        } catch (final IOException ioe) {
          // ignore
        }
      }
    } else {
      redirectAttributes.addFlashAttribute(ATTR_DOWNLOAD, "");
      redirectAttributes.addFlashAttribute(ATTR_MESSAGE, "Fehler beim Planen "
          + file.getOriginalFilename() + " weil Die Datei leer war");
    }

    return "redirect:/";
  }

  private static Date parseDate(final String input) throws ParseException {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return dateFormat.parse(input);
  }
}
