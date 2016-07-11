package de.mytest.spielplan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

public class SpielPlan {

	private static Logger logger = LoggerFactory.getLogger(SpielPlan.class);

	private String[] teams;

	private Date startDate;

	public SpielPlan(Date startDate, String[] teams) {
		this.startDate = startDate;
		this.teams = teams;
	}

	public List<String> plan() {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.startDate);
		final int teamsSize = this.teams.length;
		final List<String> allGames = new ArrayList<>();
		final List<String> awayGames = new ArrayList<>();
		final int gamesProRound = (teamsSize - 1) / 2;
		final int loopCount;
		final boolean eventCount;
		if (teamsSize % 2 == 0) {
			loopCount = teamsSize - 1;
			eventCount = true;
		} else {
			loopCount = teamsSize;
			eventCount = false;
		}
		for (int i = 1; i <= loopCount; i++) {
			final String date = printDate(calendar.getTime());
			for (int j = 1; j <= gamesProRound; j++) {
				final String team1 = this.teams[mod(i + j, loopCount) - 1];
				final String team2 = this.teams[mod(i - j, loopCount) - 1];
				allGames.add(date + " | " + team1 + " - " + team2);
				awayGames.add(team2 + " - " + team1);
				logger.debug(mod(i + j, loopCount) + "," + mod(i - j, loopCount));
			}
			if (eventCount) {
				final String team1 = this.teams[i - 1];
				final String team2 = this.teams[loopCount];
				allGames.add(date + " | " + team1 + " - " + team2);
				awayGames.add(team2 + " - " + team1);
				logger.debug(i + "," + teamsSize);
			}
			awayGames.add("");
			calendar.add(Calendar.DATE, 7);
		}

		for (final String game : awayGames) {
			if ("".equals(game)) {
				calendar.add(Calendar.DATE, 7);
			} else {
				final String date = printDate(calendar.getTime());
				allGames.add(date + " | " + game);
			}
		}

		return allGames;
	}

	private static String printDate(final Date date) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("E dd.MM.yyyy", LocaleContextHolder.getLocale());
		return dateFormat.format(date);
	}

	private static int mod(final int x, final int y) {
		final int result = Math.floorMod(x, y);
		if (result == 0) {
			return y;
		}
		return result;
	}

}
