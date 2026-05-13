package com.example.bijlibachat.service;

import com.example.bijlibachat.dao.BillDAO;
import com.example.bijlibachat.dao.MeterReadingDAO;
import com.example.bijlibachat.model.ElectricityBill;
import java.time.LocalDate;

public class BillingService {
    private BillDAO billDAO = new BillDAO();
    private MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private static final float GST_RATE = 0.17f;

    public float calculateAndRecordBill(int userID, float units, String accountType) {
        TariffEngine engine = new TariffEngine(accountType);
        float baseAmount = engine.applyTariffStructure(units);
        float withFixed = baseAmount + engine.getFixedCharge(units);
        float withGST = withFixed + (withFixed * GST_RATE);
        float finalAmount = Math.round(withGST * 100.0f) / 100.0f;

        ElectricityBill bill = new ElectricityBill();
        bill.setUserID(userID);
        bill.setEstimatedAmount(finalAmount);
        bill.setBillingCycle(LocalDate.now().getMonth().toString() + " " + LocalDate.now().getYear());
        bill.setStatus("estimated");
        billDAO.saveOrUpdateBill(bill);

        return finalAmount;
    }

    public float includeFixedChargesAndTaxes(float baseAmount, float fixedCharge) {
        float withFixed = baseAmount + fixedCharge;
        return withFixed + (withFixed * GST_RATE);
    }

    public ElectricityBill getLatestBill(int userID) {
        return billDAO.getLatestBill(userID);
    }

    public java.util.List<ElectricityBill> getBillingHistory(int userID) {
        return billDAO.getBillingHistory(userID);
    }
}
