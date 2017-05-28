package com.games.potato.mathbyheart.Activities;

import android.app.Activity;
import android.renderscript.ScriptGroup;

import com.games.potato.mathbyheart.R;
import com.games.potato.mathbyheart.math.Math;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon on 2017-04-16.
 */

@Root(name = "formulas")
public class Formulas {
    /* Elements */
    @ElementList(required = false, inline = true)
    private ArrayList<Formula> list;

    @Element(name = "table", required = false)
    private Table table;


    @ElementList(name = "starredList", required = false)
    private ArrayList<String> starredList;


    private boolean questionsFirst = true; /* Should show questions first */
    private boolean showingQuestion = true; /* Last formula returned was a question */

    private File file;

    public Formulas() {
        this.list = new ArrayList<>();
    }

    public static Formulas read(File file) {
        try {
            Serializer ser = new Persister();
            Formulas formulas = ser.read(Formulas.class, file);
            formulas.file = file;
            return formulas;
        } catch (Exception e) {
            Math.print("ERROR in Formulas.java read(): " + e.toString());
            return null;
        }

    }


    /* Getters & Setters */
    public String getFormulaString(int id) {
        return getFormulaString(id, questionsFirst);
    }

    public String getFormulaString(int id, boolean showQuestion) {
        showingQuestion = showQuestion;
        if (showQuestion) {
            return getFormula(id).getQuestion();
        } else {
            return getFormula(id).getAnswer();
        }
    }

    public Formula getFormula(int id) {
        if (table != null) {
            if (list.isEmpty()) {
                String operation = "";
                String answer = "";
                for (int x = table.getStartNumber(); x <= table.getEndNumber(); x++) {
                    for (int y = table.getStartNumber(); y <= table.getEndNumber(); y++) {
                        operation = "$$" + x + table.getOperation() + y + "$$";
                        answer = "$$" + table.getAnswer(x, y).toString() + "$$";
                        list.add(new Formula(
                                operation,
                                answer,
                                starredList.contains(operation)
                        ));
                        //TODO: make table entries starrable
                    }
                }
            }
        }
        return list.get(id);
    }


    public String getOtherSide(int id) {
        return getFormulaString(id, !showingQuestion);
    }

    public void setQuestionsFirst(boolean questionsFirst) {
        this.questionsFirst = questionsFirst;
    }

    public void toggleStarred(int id, File starredFile) {
        setStarred(id, !isStarred(id));
        try {
            Serializer serializer = new Persister();
            if (table != null) {
                /* If table exists, we must add the starred formulas at the end of the table XML file */
                if (!isStarred(id)) {
                    starredList.remove(getFormula(id).getQuestion());
                } else {
                    starredList.add(getFormula(id).getQuestion());
                }
                serializer.write(this, file);
            } else {
                serializer.write(this, file);
            }


            /* Write changes to starredFormulas file */
            Formulas formula = Formulas.read(starredFile);
            if (formula == null) {
            /*If the file doesn't exist, create a new formula object to write in a new XML file */
                formula = new Formulas();
            } else {
            /* Delete old file if it exists (it will be replaced with an updated version */
                starredFile.delete();
            }

            try {
            /* Create the file */
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(starredFile));
                bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<formulas>\n" +
                        "</formulas>"
                );//TODO: Change
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception e) {
                System.err.println("Couldn't create file -> Formulas.java toggleStarred()\n" + e.toString());
            }

            if (isStarred(id)) {
                if (!formula.list.contains(getFormula(id))) {
                    formula.list.add(getFormula(id));
                }
            } else if(formula.getFormula(id).equals(this.getFormula(id))){
                formula.list.remove(id);
            }
            serializer.write(formula, starredFile);

        } catch (Exception e) {
            Math.print(e.toString());
        }
    }


    public void setStarred(int id, boolean starred) {
        list.get(id).setStarred(starred);
    }

    public boolean isStarred(int id) {
        return list.get(id).isStarred();
    }


    public ArrayList<String> getStarredList() {
        return starredList;
    }

    public void setStarredList(ArrayList<String> starredList) {
        this.starredList = starredList;
    }


    /* Formula Class */
    public static class Formula {
        @Element(name = "question")
        private String question;
        @Element(name = "answer")
        private String answer;



        @Element(name = "starred")
        private boolean starred;


        public Formula(String question, String answer, boolean starred) {
            this.question = question;
            this.answer = answer;
            this.starred = starred;
        }

        private Formula() {
        }

        /* Getters & Setters */

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public boolean isStarred() {
            return starred;
        }

        public void setStarred(boolean starred) {
            this.starred = starred;
        }

        @Override
        public String toString() {
            return "Formula{" +
                    "question='" + question + '\'' +
                    ", answer='" + answer + '\'' +
                    ", starred=" + starred +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Formula formula = (Formula) o;

            if (starred != formula.starred) return false;
            if (question != null ? !question.equals(formula.question) : formula.question != null)
                return false;
            return answer != null ? answer.equals(formula.answer) : formula.answer == null;

        }

    }


    /* Table Class */
    public static class Table {
        @Element(name = "operation")
        private String operation;
        @Element(name = "startNumber")
        private int startNumber;
        @Element(name = "endNumber")
        private int endNumber;

        public Table(String operation, int startNumber, int endNumber) {
            Math.print("TABLE");
            this.operation = operation;
            this.startNumber = startNumber;
            this.endNumber = endNumber;
        }

        private Table() {
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Double getAnswer(double number1, double number2) {
            switch (operation) {
                case "\\times":
                    return number1 * number2;
                case "\\div":
                    return number1 / number2;
                case "+":
                    return number1 + number2;
                case "-":
                    return number1 - number2;
            }
            return new Double(0);//TODO
        }

        public int getStartNumber() {
            return startNumber;
        }

        public void setStartNumber(int startNumber) {
            this.startNumber = startNumber;
        }

        public int getEndNumber() {
            return endNumber;
        }

        public void setEndNumber(int endNumber) {
            this.endNumber = endNumber;
        }
    }

}
