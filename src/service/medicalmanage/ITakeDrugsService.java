package service.medicalmanage;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface ITakeDrugsService {
    /**
     * 药房取药
     */
    public List takeDrugs(int CaseNumber, Date PrescriptionTime, int State) throws SQLException;
    /**
     * 发药退药
     */
    public void freshPrescription(int State, int PrescriptionID) throws SQLException;

    /**
     * 改变药方状态
     * @param State
     * @param ID
     */
    public void changeState(int State,String[] ID) throws SQLException;
}
