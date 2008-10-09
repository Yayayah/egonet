/***
 * Copyright (c) 2008, Endless Loop Software, Inc.
 * 
 * This file is part of EgoNet.
 * 
 * EgoNet is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EgoNet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.endlessloopsoftware.ego;
import java.util.Date;

import com.endlessloopsoftware.egonet.data.QuestionLinkDataValue;
import com.endlessloopsoftware.egonet.interfaces.QuestionEJBPK;
import com.endlessloopsoftware.egonet.util.QuestionDataValue;
import com.endlessloopsoftware.egonet.util.SelectionDataValue;

import org.egonet.exceptions.MalformedQuestionException;
import org.egonet.util.listbuilder.Selection;

import electric.xml.Element;
import electric.xml.Elements;

/*******************************************************************************
 * Routines for creating and handling atomic question elements
 */
public class Question implements Cloneable {
	public boolean centralMarker = false;

	public boolean statable = false;

	public Long UniqueId = new Long(new Date().getTime());

	public int questionType = Question.EGO_QUESTION;

	public String title = "";

	public String text = "";

	public String citation = "";

	public int answerType = Question.TEXT;

	public int numQAlters = -1;

	public QuestionLink link = new QuestionLink();

	private Selection[] selections = new Selection[0];

	public Answer answer = new Answer(new Long(-1));

	/* Constants */
	public static final int MIN_QUESTION_TYPE = 1;

	public static final int STUDY_CONFIG = 0;

	public static final int EGO_QUESTION = 1;

	public static final int ALTER_PROMPT = 2;

	public static final int ALTER_QUESTION = 3;

	public static final int ALTER_PAIR_QUESTION = 4;

	public static final int NUM_QUESTION_TYPES = 5;

	public static final int ALL_QUESTION_TYPES = 5;

	public static final int MAX_QUESTION_TYPE = 4;

	public static final int MIN_ANSWER_TYPE = 0;

	public static final int CATEGORICAL = 0;

	public static final int NUMERICAL = 1;

	public static final int TEXT = 2;

	public static final int MAX_ANSWER_TYPE = 2;

	public static final int MAX_CATEGORICAL_CHOICES = 9;

	public static final String[] questionName = { "Study", "Ego",
			"Alter Prompt", "Alter", "Alter Pair" };

	/***************************************************************************
	 * Creates question
	 * 
	 * @return question new question
	 */
	public Question() {
	}

	/***************************************************************************
	 * Creates question with string as title
	 * 
	 * @param s
	 *            question title
	 * @return question new question
	 */
	public Question(String s) {
		this.title = s;
	}

	/***************************************************************************
	 * Converts a QuestionDataValue to a question object
	 * 
	 * @param question
	 *            XML element of question
	 * @param base
	 *            whether this is from the base file
	 * @throws MalformedQuestionException
	 *             if theres is a problem with the XML representation
	 */
	public Question(QuestionDataValue data) {
		this.UniqueId = data.getId();
		this.questionType = data.getQuestionType();
		this.answerType = data.getAnswerType();
		this.title = data.getTitle();
		this.text = data.getText();
		this.citation = data.getCitation();

		SelectionDataValue[] selectionData = data.getSelectionDataValues();

		/* Fix to properly display Apple UI interviews */
		if (this.questionType == Question.ALTER_PROMPT) {
			this.answerType = Question.TEXT;
		}

		/*
		 * temp vars for determining statable, a question must have at least one
		 * of each selection type to be statable
		 */
		boolean adjacent = false;
		boolean nonadjacent = false;

		this.setSelections(new Selection[selectionData.length]);
		for (int i = 0; i < selectionData.length; ++i) {

			Selection selection = new Selection();
			selection.setString(selectionData[i].getText());
			selection.setIndex(selectionData[i].getIndex());
			selection.setValue(selectionData[i].getValue());
			selection.setAdjacent(selectionData[i].getAdjacent());

			if (selection.isAdjacent())
				adjacent = true;
			else
				nonadjacent = true;

			getSelections()[i] = selection;
		}

		/*
		 * a question must have at least one of each selection type to be
		 * statable
		 */
		this.statable = adjacent && nonadjacent;

		if (data.getQuestionLinkDataValue() != null) {
			QuestionLinkDataValue qlData = data.getQuestionLinkDataValue();

			this.link.active = true;
			this.link.answer = new Answer(qlData.getQuestionId());
			this.link.answer.setValue(qlData.getAnswerValue());
			this.link.answer.string = qlData.getAnswerString();
		} else {
			this.link.active = false;
		}
	}

	/***************************************************************************
	 * Reads a single question from an input stream
	 * 
	 * @param question
	 *            XML element of question
	 * @param base
	 *            whether this is from the base file
	 * @throws MalformedQuestionException
	 *             if theres is a problem with the XML representation
	 */
	public Question(Element question) throws MalformedQuestionException {
//		if ((question.getElement("QuestionTitle") == null)
//				|| (question.getElement("QuestionText") == null)
//				|| (question.getElement("Id") == null)
//				|| (question.getElement("QuestionType") == null)
//				|| (question.getElement("AnswerType") == null)) {
//			System.out.println("title:" + question.getElement("QuestionTitle")
//					+ "ID:" + question.getElement("Id"));
//			throw (new MalformedQuestionException());
//		}
		
		if(question.getElement("QuestionTitle") == null) {
			this.title = "";
		} else if(question.getElement("QuestionText") == null) {
			this.text = "";
		} 
		
		this.title = question.getTextString("QuestionTitle");
		this.title = (this.title == null) ? "" : this.title;

		this.text = question.getTextString("QuestionText");
		this.text = (this.text == null) ? "" : this.text;

		this.citation = question.getTextString("Citation");
		this.citation = (this.citation == null) ? "" : this.citation;

		this.UniqueId = new Long(question.getLong("Id"));
		this.questionType = question.getInt("QuestionType");
		this.answerType = question.getInt("AnswerType");

		if (this.questionType == Question.ALTER_PROMPT) {
			this.answerType = Question.TEXT;
		}

		if (question.getAttribute("CentralityMarker") != null) {
			boolean centrality = question.getAttribute("CentralityMarker")
					.equals("true");

			if (centrality
					&& (this.questionType != Question.ALTER_PAIR_QUESTION)) {
				System.out.println("ID:" + this.UniqueId + " title:"
						+ this.title);
				throw (new MalformedQuestionException());
			}
		}

		Element link = question.getElement("Link");
		if (link != null) {
			this.link.active = true;
			this.link.answer = new Answer(new Long(link.getLong("Id")));
			this.link.answer.setValue(link.getInt("value"));

			/* Only support questions with single answers for link */
			this.link.answer.string = link.getTextString("string");
		}

		if (this.answerType == Question.CATEGORICAL) {
			Element answerList = question.getElement("Answers");

			if (answerList != null) {
				Elements selections = answerList.getElements("AnswerText");

				if (selections.size() == 0) {
					throw (new MalformedQuestionException());
				}

				/*
				 * temp vars for determining statable, a question must have at
				 * least one of each selection type to be statable
				 */
				boolean adjacent = false;
				boolean nonadjacent = false;

				this.setSelections(new Selection[selections.size()]);

				while (selections.hasMoreElements()) {

					Element selection = selections.next();
					int index = Integer.parseInt(selection
							.getAttributeValue("index"));

					try {
						this.getSelections()[index] = new Selection();
						this.getSelections()[index].setString(selection
								.getTextString());
						this.getSelections()[index]
								.setValue(Integer.parseInt(selection
										.getAttributeValue("value")));

						this.getSelections()[index].setAdjacent(Boolean.valueOf(
								selection.getAttributeValue("adjacent"))
								.booleanValue());
						this.getSelections()[index].setIndex(index);

					} catch (NumberFormatException ex) {
						System.out.println("Throwing exception");
						this.getSelections()[index].setValue(selections.size()
								- (index + 1));
						this.getSelections()[index].setAdjacent(false);
					}

					if (this.getSelections()[index].isAdjacent())
						adjacent = true;
					else
						nonadjacent = true;
				}

				/*
				 * a question must have at least one of each selection type to
				 * be statable
				 */
				this.statable = adjacent && nonadjacent;

				/* Check to make sure all answers are contiguous */
				for (int i = 0; i < selections.size(); i++) {
					if (this.getSelections()[i] == null) {
						throw (new MalformedQuestionException());
					}
				}
			}
		}
	}

	/***************************************************************************
	 * Returns whether a given selection is adjacent based on the values stored
	 * in the question. Is used to override value found in an interview file
	 * 
	 * @param value
	 * @return true iff that selection is marked as adjacent
	 */
	public boolean selectionAdjacent(int value) {
		boolean rval = false;

		if (this.getSelections().length > 0) {
			int size = this.getSelections().length;

			for (int i = 0; i < size; i++) {
				if (value == this.getSelections()[i].getValue()) {
					rval = this.getSelections()[i].isAdjacent();
					break;
				}
			}
		}

		return rval;
	}

	/***************************************************************************
	 * Returns String representation of questionType
	 * 
	 * @param type
	 *            Question type to return as string
	 * @return string Question Type string
	 */
	public static String questionTypeString(int type) {
		return (new String(questionName[type]));
	}

	/***************************************************************************
	 * Overrides toString method for question, returns title
	 * 
	 * @return String title of question
	 */
	public String toString() {
		if (title == null) {
			return (new String("Untitled"));
		} else {
			return (title);
		}
	}

	/***************************************************************************
	 * Writes a single question to an output stream
	 * 
	 * @param w
	 *            Print Writer of open output file
	 * @param q
	 *            question
	 */
	public void writeQuestion(Element e, QuestionList list) {
		if (this.centralMarker) {
			e.setAttribute("CentralityMarker", "true");
		}

		e.addElement("Id").setLong(this.UniqueId.longValue());
		e.addElement("QuestionType").setInt(this.questionType);
		e.addElement("AnswerType").setInt(this.answerType);

		if ((this.title != null) && (!this.title.equals(""))) {
			e.addElement("QuestionTitle").setText(this.title);
		}

		if ((this.text != null) && (!this.text.equals(""))) {
			e.addElement("QuestionText").setText(this.text);
		}

		if ((this.citation != null) && (!this.citation.equals(""))) {
			e.addElement("Citation").setText(this.citation);
		}

		if (this.getSelections().length > 0) {
			int size = this.getSelections().length;
			Element selections = e.addElement("Answers");

			for (int i = 0; i < size; i++) {
				Element answer = selections.addElement("AnswerText");
				answer.setText(this.getSelections()[i].getString());
				answer.setAttribute("index", Integer.toString(i));
				answer.setAttribute("value", Integer
						.toString(this.getSelections()[i].getValue()));
				answer.setAttribute("adjacent",
						this.getSelections()[i].isAdjacent() ? "true" : "false");
			}
		}

		if (this.link.active
				&& (list.getQuestion(this.link.answer.questionId) != null)) {
				Element link = e.addElement("Link");
				link.addElement("Id").setLong(
						this.link.answer.questionId.longValue());
				link.addElement("value").setInt(this.link.answer.getValue());
				link.addElement("string").setText(this.link.answer.string);
		}
	}

	public QuestionDataValue getDataValue(QuestionList list, Long studyId) {
		QuestionEJBPK pk = new QuestionEJBPK(this.UniqueId, studyId);
		QuestionDataValue data = new QuestionDataValue(pk);

		data.setId(this.UniqueId);
		data.setQuestionType(this.questionType);
		data.setAnswerType(this.answerType);
		data.setTitle(this.title);
		data.setText(this.text);
		data.setCitation(this.citation);

		if (this.getSelections().length > 0) {
			int size = this.getSelections().length;

			for (int i = 0; i < size; i++) {
				SelectionDataValue selectionData = new SelectionDataValue();
				selectionData.setText(this.getSelections()[i].getString());
				selectionData.setIndex(i);
				selectionData.setValue(this.getSelections()[i].getValue());
				selectionData.setAdjacent(this.getSelections()[i].isAdjacent());

				data.addSelectionDataValue(selectionData);
			}
		}

		if (this.link.active
				&& (list.getQuestion(this.link.answer.questionId) != null)) {
				QuestionLinkDataValue qlData = new QuestionLinkDataValue();
				qlData.setActive(true);
				qlData.setAnswerValue(this.link.answer.getValue());
				qlData.setAnswerString(this.link.answer.string);
				qlData.setQuestionId(this.link.answer.questionId);
		}

		return data;
	}

	/***************************************************************************
	 * Implements Clone interface
	 * 
	 * @return Clone of Question
	 */
	public Object clone() {
		Question q;

		try {
			q = (Question) super.clone();
			q.link = (QuestionLink) this.link.clone();

			/*******************************************************************
			 * Dangerous to clone answers as multiple answers refer to same
			 * question Make sure they are assigned explicitly
			 */
			q.answer = null;
		} catch (CloneNotSupportedException ex) {
			q = null;
		}

		return q;
	}

	public String getString() {
		String str = "";
		str = "ID : " + UniqueId + " Title : " + title + " text : " + text
				+ "\nAnswer : " + answer.getString();
		return str;
	}

	public void setSelections(Selection[] selections) {
		this.selections = selections;
	}

	public Selection[] getSelections() {
		return selections;
	}
}
