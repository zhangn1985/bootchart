/*
 * Bootchart -- Boot Process Visualization
 *
 * Copyright (C) 2004  Ziga Mahkovec <ziga.mahkovec@klika.si>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bootchart.parser.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bootchart.common.Common;
import org.bootchart.common.MEMSample;
import org.bootchart.common.Sample;
import org.bootchart.common.Stats;


/**
 * MEMParser parses log files produced by logging the output of
 * <code>/proc/meminfo</code>.
 */
public class MEMParser {
	private static final Logger log = Logger.getLogger(CPUFreqParser.class.getName());

	private static final String TOTAL_String = "MemTotal:";
	private static final String FREE_String = "MemFree:";
	private static final String Available_String = "MemAvailable:";

	public static Stats parseLog(InputStream is)
		throws IOException {
		BufferedReader reader = Common.getReader(is);
		String line = reader.readLine();

		int numSamples = 0;
		Stats memStats = new Stats();
		// last time
		Date ltime = null;

		long total, free, available;

		while (line != null) {
			// skip empty lines
			while (line != null && line.trim().length() == 0) {
				line = reader.readLine();
			}
			if (line == null) {
				// EOF
				break;
			}
			line = line.trim();

			if (line.startsWith("#")) {
				continue;
			}
			Date time = null;
			if (line.matches("^\\d+$")) {
				time = new Date(Long.parseLong(line) * 10);
				total = free = available = 0;
				numSamples++;
			} else {
				line = reader.readLine();
				continue;
			}

			// read meminfo
			line = reader.readLine();
			while (line != null && line.trim().length() > 0) {
				line = line.trim();
				String[] tokens = line.split("\\s+");
				if (line.startsWith(TOTAL_String))
					total = Long.parseLong(tokens[1]);
				else if (line.startsWith(FREE_String))
					free = Long.parseLong(tokens[1]);
				else if (line.startsWith(Available_String))
					available = Long.parseLong(tokens[1]);
				line = reader.readLine();
			}

			MEMSample memSample = new MEMSample(time, (double)free/total, (double)available/total);
			memStats.addSample(memSample);

			if (numSamples > Common.MAX_PARSE_SAMPLES) {
				break;
			}
		}
		log.fine("Parsed " + memStats.getSamples().size() + " mem samples");
		return memStats;
	}
}
