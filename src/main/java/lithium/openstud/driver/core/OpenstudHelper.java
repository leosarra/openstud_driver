package lithium.openstud.driver.core;

import lithium.openstud.driver.core.models.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.threeten.bp.LocalDate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpenstudHelper {

    public enum Mode {
        MOBILE, WEB
    }

    public enum Provider {
        SAPIENZA
    }

    public static boolean isValidUrl(String url) {
        if (url == null) return false;
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(url);
    }

    public static double computeWeightedAverage(List<ExamDone> list, int laude) {
        double cfu = 0;
        double sum = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed() && exam.getResult() >= 18) {
                int grade = exam.getResult();
                if (grade == 31) grade = laude;
                sum += grade * exam.getCfu();
                cfu += exam.getCfu();
            }
        }
        if (cfu == 0 || sum == 0) return 0;
        return sum / cfu;
    }

    public static int computeBaseGraduation(List<ExamDone> list, int laude) {
        double result = (computeWeightedAverage(list, laude) * 110) / 30;
        return (int) Math.ceil(result);
    }

    public static double computeArithmeticAverage(List<ExamDone> list, int laude) {
        int num = 0;
        double sum = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed() && exam.getResult() >= 18) {
                int grade = exam.getResult();
                if (grade == 31) grade = laude;
                sum += grade;
                num++;
            }
        }
        if (num == 0 || sum == 0) return -1;
        return sum / num;
    }

    public static int getSumCFU(List<ExamDone> list) {
        int cfu = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed()) {
                cfu += exam.getCfu();
            }
        }
        return cfu;
    }

    public static ExamDone createFakeExamDone(String description, int cfu, int grade) {
        if (cfu <= 0 || grade < 18) return null;
        ExamDone done = new ExamDone();
        done.setCertified(true);
        done.setPassed(true);
        done.setCfu(cfu);
        done.setDate(LocalDate.now());
        done.setDescription(description);
        if (grade >= 31) done.setResult(31);
        else done.setResult(grade);
        return done;
    }

    public static List<Event> generateEventsFromTimetable(Map<String, List<Lesson>> timetable) {
        List<Event> events = new LinkedList<>();
        for (String code : timetable.keySet()) {
            List<Lesson> lessons = timetable.get(code);
            for (Lesson lesson : lessons) {
                Event ev = new Event(lesson.getName(), lesson.getStart(), lesson.getEnd(), EventType.LESSON);
                ev.setTeacher(lesson.getTeacher());
                ev.setWhere(lesson.getWhere());
                events.add(ev);
            }
        }
        return events;
    }

    public static List<Event> generateEventsFromTimetable(List<Lesson> timetable) {
        List<Event> events = new LinkedList<>();
        for (Lesson lesson : timetable) {
            Event ev = new Event(lesson.getName(), lesson.getStart(), lesson.getEnd(), EventType.LESSON);
            ev.setTeacher(lesson.getTeacher());
            ev.setWhere(lesson.getWhere());
            events.add(ev);
        }
        return events;
    }

    public static List<Event> generateEvents(List<ExamReservation> reservations,
                                             List<ExamReservation> avaiableReservations) {
        List<Event> events = new LinkedList<>();
        for (ExamReservation res : reservations) {
            Event ev = new Event(res.getExamSubject(), res.getExamDate().atStartOfDay(), null, EventType.RESERVED);
            ev.setWhere(res.getNote());
            ev.setTeacher(res.getTeacher());
            ev.setExamDate(res.getExamDate());
            ev.setReservation(res);
            events.add(ev);
        }
        for (ExamReservation res : avaiableReservations) {
            boolean exist = false;
            for (ExamReservation res_active : reservations) {
                if (res_active.getReportID() == res.getReportID() && res_active.getSessionID() == res.getSessionID()) {
                    exist = true;
                    break;
                }
            }
            if (exist) continue;
            Event event = new Event(res.getExamSubject(), res.getExamDate().atStartOfDay(), null, EventType.DOABLE);
            event.setStartReservations(res.getStartDate());
            event.setEndReservations(res.getEndDate());
            event.setTeacher(res.getTeacher());
            event.setWhere(res.getNote());
            event.setExamDate(res.getExamDate());
            event.setReservation(res);
            events.add(event);
        }
        return events;
    }

    public static List<ExamDone> sortExamByDate(List<ExamDone> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getDate() == null && o2.getDate() == null) return 0;
            if (ascending)
                if (o1.getDate() == null) return 1;
                else if (o2.getDate() == null) return -1;
                else return o1.getDate().compareTo(o2.getDate());
            else {
                if (o1.getDate() == null) return -1;
                else if (o2.getDate() == null) return 1;
                else return o2.getDate().compareTo(o1.getDate());
            }
        });
        return list;
    }

    public static List<ExamReservation> sortReservationByDate(List<ExamReservation> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getExamDate() == null && o2.getExamDate() == null) return 0;
            if (ascending)
                if (o1.getExamDate() == null) return 1;
                else if (o2.getExamDate() == null) return -1;
                else return o1.getExamDate().compareTo(o2.getExamDate());
            else {
                if (o1.getExamDate() == null) return -1;
                else if (o2.getExamDate() == null) return 1;
                else return o2.getExamDate().compareTo(o1.getExamDate());
            }
        });
        return list;
    }

    public static List<Lesson> sortLessonsByStartDate(List<Lesson> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getStart() == null && o2.getStart() == null) return 0;
            if (ascending)
                if (o1.getStart() == null) return 1;
                else if (o2.getStart() == null) return -1;
                else return o1.getStart().compareTo(o2.getStart());
            else {
                if (o1.getStart() == null) return -1;
                else if (o2.getStart() == null) return 1;
                else return o2.getStart().compareTo(o1.getStart());
            }
        });
        return list;
    }

    public static List<ExamDone> sortExamByGrade(List<ExamDone> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (ascending)
                return Integer.compare(o1.getResult(), o2.getResult());
            else {
                return Integer.compare(o2.getResult(), o1.getResult());
            }
        });
        return list;
    }

}
