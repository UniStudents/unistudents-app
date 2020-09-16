package com.unipi.students.service;

import android.content.Context;

import com.unipi.students.common.HttpStatus;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.Student;
import com.unipi.students.parser.PANTEIONParser;
import com.unipi.students.parser.UNIPIParser;
import com.unipi.students.parser.UNIWAParser;
import com.unipi.students.parser.UOAParser;
import com.unipi.students.scraper.PANTEIONScraper;
import com.unipi.students.scraper.UNIPIScraper;
import com.unipi.students.scraper.UNIWAScraper;
import com.unipi.students.scraper.UOAScraper;

import org.jsoup.nodes.Document;

public class ScrapeService {

    public ResponseEntity getStudent(String university, String username, String password, Context context) {
        switch (university) {
            case "UNIPI":
                return getUNIPIStudent(username, password);
            case "UNIWA":
                return getUNIWAStudent(username, password);
            case "UOA":
                return getUOAStudent(username, password);
            case "PANTEION":
                return getPANTEIONStudent(username, password, context);
            default:
                return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    /*
     *
     *
     * SCRAPE SERVICES FOR UNIS
     *
     *
    */

    private ResponseEntity getUNIPIStudent(String username, String password) {
        // scrap info page
        UNIPIScraper scraper = new UNIPIScraper(username, password);

        // check for connection errors
        if (!scraper.isConnected()) {
            return new ResponseEntity(HttpStatus.REQUEST_TIMEOUT);
        }

        // authorized check
        if (!scraper.isAuthorized()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        Document infoPage = scraper.getStudentInfoPage();
        Document gradesPage = scraper.getGradesPage();

        // connection check
        if (infoPage == null || gradesPage == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UNIPIParser parser = new UNIPIParser();
        Student student = parser.parseInfoAndGradesPages(infoPage, gradesPage);

        if (student == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(student, HttpStatus.OK);
    }

    private ResponseEntity getUNIWAStudent(String username, String password) {
        // scrap info page
        UNIWAScraper scraper = new UNIWAScraper(username, password);

        // check for connection errors
        if (!scraper.isConnected()) {
            return new ResponseEntity(HttpStatus.REQUEST_TIMEOUT);
        }

        // authorization check
        if (!scraper.isAuthorized()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        String infoJSON = scraper.getInfoJSON();
        String gradesJSON = scraper.getGradesJSON();
        String totalAverageGrade = scraper.getTotalAverageGrade();

        // check for internal errors
        if (infoJSON == null || gradesJSON == null || totalAverageGrade == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UNIWAParser parser = new UNIWAParser();
        Student student = parser.parseInfoAndGradesJSON(infoJSON, gradesJSON, totalAverageGrade);

        if (student == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(student, HttpStatus.OK);
    }

    private ResponseEntity getUOAStudent(String username, String password) {
        // scrape student information
        UOAScraper scraper = new UOAScraper(username, password);

        // check for connection errors
        if (!scraper.isConnected()) {
            return new ResponseEntity(HttpStatus.REQUEST_TIMEOUT);
        }

        // authorization check
        if (!scraper.isAuthorized()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        Document infoPage = scraper.getStudentInfoPage();
        Document gradesPage = scraper.getGradesPage();
        Document declareHistoryPage = scraper.getDeclareHistoryPage();

        // check for internal errors
        if (infoPage == null || gradesPage == null || declareHistoryPage == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UOAParser parser = new UOAParser();
        Student student = parser.parseInfoAndGradesPages(infoPage, gradesPage, declareHistoryPage);

        if (student == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        student.getInfo().setAem(username);

        return new ResponseEntity(student, HttpStatus.OK);
    }

    private ResponseEntity getPANTEIONStudent(String username, String password, Context context) {
        PANTEIONScraper scraper = new PANTEIONScraper(username, password, context);

        // check for connection errors
        if (!scraper.isConnected()) {
            return new ResponseEntity(HttpStatus.REQUEST_TIMEOUT);
        }

        // authorization check
        if (!scraper.isAuthorized()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        Document[] infoAndGradesPages = scraper.getInfoAndGradesPages();

        // check for internal errors
        if (infoAndGradesPages == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        PANTEIONParser parser = new PANTEIONParser();
        Student student = parser.parseInfoAndGradesPages(infoAndGradesPages);

        if (student == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(student, HttpStatus.OK);
    }
}
