/*******************************************************************************
 * Copyright (c) 2016 Zend Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Zend Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.core.index.lucene;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utility class for Lucene indexer.
 * 
 * @author Bartlomiej Laczkowski
 */
public final class Utils {

	private Utils() {
		// Utility class, should not be instantiated.
	}

	/**
	 * Deletes directory under given path with all of its contents.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void delete(Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static String getCamelCaseName(String name) {
		String camelCaseName = null;
		StringBuilder camelCaseNameBuf = new StringBuilder();
		for (int i = 0; i < name.length(); ++i) {
			char ch = name.charAt(i);
			if (Character.isUpperCase(ch)) {
				camelCaseNameBuf.append(ch);
			} else if (i == 0) {
				// Not applicable for camel case search
				break;
			}
		}
		camelCaseName = camelCaseNameBuf.length() > 0
				? camelCaseNameBuf.toString() : null;
		return camelCaseName;
	}

}
