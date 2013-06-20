/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
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

package com.adamrosenfield.wordswithcrosses.puz;

import java.util.Calendar;

public class PuzzleMeta {

    public long id;
    public String filename;
    public boolean archived;
	public String author;
	public String title;
	public String source;
	public Calendar date;
	public int percentComplete;
	public String sourceUrl;

	@Override
    public String toString(){
		return new StringBuilder("id: ")
		.append(id)
		.append(" filename: ")
		.append(filename)
		.append(" archived: ")
		.append(archived)
		.append("author: ")
		.append(author)
		.append(" title: ")
		.append(title)
		.append(" source: ")
		.append(source)
		.append(" sourceUrl: ")
		.append(sourceUrl)
		.append(" date: " )
		.append(date)
		.append(" percentCompelete: ")
		.append(percentComplete)
		.toString();
	}
}
