package de.mytest.spielplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

	private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String provideUploadInfo(Model model) throws IOException {
		return "uploadForm";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		if (!file.isEmpty()) {
			BufferedReader input = null;
			try {
				input = new BufferedReader(new InputStreamReader(file.getInputStream(), "Cp1252"));
				final JsonReader jsonReader = Json.createReader(input);
				final JsonObject jsonObject = jsonReader.readObject();

				final String start = jsonObject.getString("start");
				Date startDate = parseDate(start);

				final JsonArray jsonArray = jsonObject.getJsonArray("teams");
				final int size = jsonArray.size();
				String[] teams = new String[size];
				for (int i = 0; i < jsonArray.size(); i++) {
					final JsonObject teamObject = jsonArray.getJsonObject(i);
					teams[i] = teamObject.getString("name");
				}

				SpielPlan spielPlan = new SpielPlan(startDate, teams);
				List<String> result = spielPlan.plan();
				redirectAttributes.addFlashAttribute("result", result);

				redirectAttributes.addFlashAttribute("message",
						"You successfully uploaded " + file.getOriginalFilename() + "!");
			} catch (Exception e) {
				log.debug(e.getMessage());
				redirectAttributes.addFlashAttribute("message",
						"Failued to upload " + file.getOriginalFilename() + " => " + e.getMessage());
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException ioe) {
					// ignore
				}
			}
		} else {
			redirectAttributes.addFlashAttribute("message",
					"Failed to upload " + file.getOriginalFilename() + " because it was empty");
		}

		return "redirect:/";
	}

	private static Date parseDate(final String input) throws ParseException {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.parse(input);
	}
}
