package com.example.bijlibachat.service;

import com.example.bijlibachat.dao.SolarDAO;
import com.example.bijlibachat.model.SolarMeter;
import java.util.Date;

public class SolarService {
    private SolarDAO solarDAO = new SolarDAO();

    public float calculateNetConsumption(float gridUnits, float solarGenerated) {
        float net = gridUnits - solarGenerated;
        return Math.max(net, 0); // cannot be negative
    }

    public float deductNetMeteringCredit(float gridUnits, float solarGenerated, float ratePerUnit) {
        float surplus = solarGenerated - gridUnits;
        if (surplus > 0) {
            return surplus * ratePerUnit; // credit for exported units
        }
        return 0;
    }

    public void saveSolarReading(int userID, float generation, float exported, float netCredit) {
        SolarMeter s = new SolarMeter();
        s.setUserID(userID);
        s.setGenerationReading(generation);
        s.setExportedUnits(exported);
        s.setNetCredit(netCredit);
        s.setReadingDate(new Date());
        solarDAO.saveSolarReading(s);
    }
    public void saveNetMeteringSnapshot(int userID, float gridUnits, float solarGenerated, float ratePerUnit) {
        float netConsumption = calculateNetConsumption(gridUnits, solarGenerated);
        float credit = deductNetMeteringCredit(gridUnits, solarGenerated, ratePerUnit);
        float exported = Math.max(solarGenerated - gridUnits, 0);
        saveSolarReading(userID, solarGenerated, exported, credit);
    }
    public SolarMeter getLatestSolarReading(int userID) {
        return solarDAO.getLatestSolarReading(userID);
    }
}
