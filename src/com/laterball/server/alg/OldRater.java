package com.laterball.server.alg;

/**
 * later-ball: 23/07/2018
 */
public class FootballDataRater {
    private static final double UPSET_ODDS_MARGIN = 2;
    private static final double UPSET_FACTOR = 7;
    private static final double GOALS_FACTOR = 50;
    private static final double YELLOW_FACTOR = 5;
    private static final double RED_FACTOR = 10;
    private static final double COMEBACK_FACTOR = 10;

    public void rate(MatchRecord matchRecord) throws RatingException {
        Statistic fthgStat = StatisticService.getInstance().getStatisticFromCode("FTHG");
        Statistic ftagStat = StatisticService.getInstance().getStatisticFromCode("FTAG");
        Statistic hthgStat = StatisticService.getInstance().getStatisticFromCode("HTHG");
        Statistic htagStat = StatisticService.getInstance().getStatisticFromCode("HTAG");
        Statistic b365HStat = StatisticService.getInstance().getStatisticFromCode("B365H");
        Statistic b365AStat = StatisticService.getInstance().getStatisticFromCode("B365A");
        Statistic ayStat = StatisticService.getInstance().getStatisticFromCode("AY");
        Statistic hyStat = StatisticService.getInstance().getStatisticFromCode("HY");
        Statistic arStat = StatisticService.getInstance().getStatisticFromCode("AR");
        Statistic hrStat = StatisticService.getInstance().getStatisticFromCode("HR");
        List<Statistic> stats = Arrays.asList(fthgStat, ftagStat, hthgStat, htagStat, b365AStat, b365HStat, ayStat,
                hyStat, arStat, hrStat);
        if (stats.stream().anyMatch(Objects::isNull)) {
            throw new RatingException("One or more required stats not found");
        }

        StatisticRecord fthgRecord = matchRecord.getStatRecordByStatId(fthgStat.getId());
        StatisticRecord ftagRecord = matchRecord.getStatRecordByStatId(ftagStat.getId());
        StatisticRecord hthgRecord = matchRecord.getStatRecordByStatId(hthgStat.getId());
        StatisticRecord htagRecord = matchRecord.getStatRecordByStatId(htagStat.getId());
        StatisticRecord b365HRecord = matchRecord.getStatRecordByStatId(b365HStat.getId());
        StatisticRecord b365ARecord = matchRecord.getStatRecordByStatId(b365AStat.getId());
        StatisticRecord ayRecord = matchRecord.getStatRecordByStatId(ayStat.getId());
        StatisticRecord hyRecord = matchRecord.getStatRecordByStatId(hyStat.getId());
        StatisticRecord arRecord = matchRecord.getStatRecordByStatId(arStat.getId());
        StatisticRecord hrRecord = matchRecord.getStatRecordByStatId(hrStat.getId());

        List<StatisticRecord> records = Arrays.asList(fthgRecord, ftagRecord, hthgRecord, htagRecord, b365HRecord,
                b365ARecord, ayRecord, hyRecord, arRecord, hrRecord);
        if (records.stream().anyMatch(Objects::isNull)) {
            throw new RatingException("One or more required records not found");
        }

        int fthg = (int) fthgRecord.getValue();
        int ftag = (int) ftagRecord.getValue();
        int hthg = (int) hthgRecord.getValue();
        int htag = (int) htagRecord.getValue();
        double b365h = (double) b365HRecord.getValue();
        double b365a = (double) b365ARecord.getValue();
        int ay = (int) ayRecord.getValue();
        int hy = (int) hyRecord.getValue();
        int ar = (int) arRecord.getValue();
        int hr = (int) hrRecord.getValue();

        double goalFactor = GOALS_FACTOR*(fthg + ftag);
        boolean isUpset = (ftag <= fthg && b365h - b365a > UPSET_ODDS_MARGIN) ||
                (ftag >= fthg && b365a - b365h > UPSET_ODDS_MARGIN);
        double upsetFactor = 0;
        if (isUpset) {
            upsetFactor = UPSET_FACTOR*Math.abs(b365h - b365a)*(1+Math.abs(ftag - fthg));
        }
        double bookingFactor = (hy + ay)*YELLOW_FACTOR + (hr + ar)*RED_FACTOR;

        double htDifference = hthg - htag;
        double ftDifference = fthg - ftag;
        double comebackFactor = (htDifference > 0 && ftDifference <= 0) || (htDifference < 0 && ftDifference >= 0) ?
                Math.abs(htDifference - ftDifference) * COMEBACK_FACTOR : 0;

        matchRecord.setRating(goalFactor + upsetFactor + bookingFactor + comebackFactor);
    }
}
