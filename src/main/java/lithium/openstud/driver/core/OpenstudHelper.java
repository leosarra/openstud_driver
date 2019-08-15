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
        if (cfu == 0 || sum == 0) return -1;
        return sum / cfu;
    }

    public static int computeBaseGraduation(List<ExamDone> list, int laude, boolean removeMaxMin) {
        if (list.size() == 0 || (removeMaxMin && list.size()<=2)) return -1;
        List<ExamDone> tmp = new LinkedList<>(list);
        if (removeMaxMin) {
            ExamDone max = null;
            ExamDone min = null;
            for (ExamDone exam:tmp) {
                if (max == null) max = exam;
                else if (exam.getResult()>max.getResult()) max = exam;
            }
            tmp.remove(max);
            for (ExamDone exam:tmp) {
                if (min == null) min = exam;
                else if (exam.getResult()<min.getResult()) min = exam;
            }
            tmp.remove(min);
        }
        double result = (computeWeightedAverage(tmp, laude) * 110) / 30;
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
                Event ev = new Event(EventType.LESSON);
                ev.setTitle(lesson.getName());
                ev.setStart(lesson.getStart());
                ev.setEnd(lesson.getEnd());
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
            Event ev = new Event(EventType.LESSON);
            ev.setTitle(lesson.getName());
            ev.setStart(lesson.getStart());
            ev.setEnd(lesson.getEnd());
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
            Event ev = new Event(EventType.RESERVED);
            ev.setTitle(res.getExamSubject());
            ev.setTeacher(res.getTeacher());
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
            Event event = new Event(EventType.DOABLE);
            event.setTitle(res.getExamSubject());
            event.setTeacher(res.getTeacher());
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
