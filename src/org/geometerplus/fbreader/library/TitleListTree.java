/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.library;

import java.util.*;

import org.geometerplus.fbreader.book.*;

public class TitleListTree extends FirstLevelTree {
	private boolean myGroupByFirstLetter;

	TitleListTree(RootTree root) {
		super(root, ROOT_BY_TITLE);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();

		myGroupByFirstLetter = false;

		List<String> letters = null;
		if (Collection.size() > 9) {
			letters = Collection.firstTitleLetters();
			myGroupByFirstLetter = Collection.size() > letters.size() * 5 / 4;
		}

		if (myGroupByFirstLetter) {
			for (String l : letters) {
				createTitleSubTree(l);
			}
		} else {
			for (Book b : Collection.books()) {
				createBookWithAuthorsSubTree(b);
			}
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		if (book == null) {
			return false;
		}
		switch (event) {
			case Added:
				if (myGroupByFirstLetter) {
					return createTitleSubTree(book.firstTitleLetter());
				} else {
					return createBookWithAuthorsSubTree(book);
				}
			case Removed:
				if (myGroupByFirstLetter) {
					// TODO: remove old tree (?)
					return false;
				} else {
					return super.onBookEvent(event, book);
				}
			default:
			case Updated:
				if (myGroupByFirstLetter) {
					// TODO: remove old tree (?)
					return createTitleSubTree(book.firstTitleLetter());
				} else {
					boolean changed = removeBook(book);
					changed |= createBookWithAuthorsSubTree(book);
					return changed;
				}
		}
	}

	private boolean createTitleSubTree(String prefix) {
		if (prefix == null) {
			return false;
		}
		final TitleTree temp = new TitleTree(Collection, prefix);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new TitleTree(this, prefix, - position - 1);
			return true;
		}
	}
}
