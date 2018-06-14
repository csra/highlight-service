/*
 * Copyright (C) 2017 pholthau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.citec.csra.highlight;

import de.citec.csra.highlight.cfg.TargetObject;
import de.citec.csra.task.cli.RemoteTask;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.InitializeException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.hri.HighlightTargetType.HighlightTarget;
import static rst.hri.HighlightTargetType.HighlightTarget.Modality.GAZE;
import rst.timing.DurationType.Duration;

/**
 *
 * @author pholthau
 */
public class HighlightRequest {

	private final static Logger LOG = Logger.getLogger(HighlightRequest.class.getName());

	static {
		DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(HighlightTarget.getDefaultInstance()));
	}

	public static void main(String[] args) throws InterruptedException {
		try {
			ExecutorService ex = Executors.newCachedThreadPool();
			HighlightTarget hlt = HighlightTarget.newBuilder().setDuration(Duration.newBuilder().setTime(6000)).addModality(GAZE).setTargetId(TargetObject.FLOBI.name()).build();
			RemoteTask t = new RemoteTask("/citec/csra/home/highlight", hlt);
			RemoteTask t2 = new RemoteTask("/citec/csra/home/highlight", "ENTRANCE,GAZE,GESTURE,7000");

			Set<Future> fs = new HashSet<>();

			fs.add(ex.submit(t));
			Thread.sleep(3000);

			fs.add(ex.submit(t2));

			ex.submit(() -> {
				System.out.println("Waiting for tasks to finish...");
				while (!fs.isEmpty()) {
					fs.forEach((Future ft) -> {
						try {
							Object result = ft.get(100, TimeUnit.MILLISECONDS);
							System.out.println("Task finished successfully with: " + result.toString().replaceAll("\n", " "));
						} catch (TimeoutException x) {
								
						} catch (ExecutionException | InterruptedException x) {
							System.out.println("error: " + x.getCause().getMessage());
							Logger.getLogger(HighlightRequest.class.getName()).log(Level.SEVERE, null, x);
						}
					});
					fs.removeIf((Future ft) -> {
						return ft.isDone();
					});

				}
				ex.shutdown();
			});

		} catch (InitializeException ex) {
			LOG.log(Level.SEVERE, "Could not initialize remote task", ex);
		}
	}

}
