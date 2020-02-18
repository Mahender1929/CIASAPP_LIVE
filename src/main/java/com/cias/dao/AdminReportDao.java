package com.cias.dao;

import java.util.List;

import com.cias.entity.Banks;
import com.cias.entity.QueryData;
import com.cias.entity.RequiredField;
import com.cias.entity.TableMetaData;
import com.cias.entity.TellerMaster;
import com.cias.entity.ViewInfo;

public interface AdminReportDao {

	public List<TableMetaData> populateDataTable(QueryData getTableData,String bank,TellerMaster master);
	public String populateQuery(QueryData table, String param, String crit);
	public RequiredField populateFields(String table);
	public void saveFavouriteQuery(QueryData data);
	public List<QueryData> retrieveFavouriteList(String data);
	public List<QueryData> getReportDetails(int id);
	public Banks  retreiveBankNames();
	//public List<String>  joinsList();
	public Banks retreiveDbConnection(String bank);
	public List<ViewInfo> retreiveViewDetails(String type);
	public String populateBankType(String bankcode);
	public boolean deleteviews(QueryData qrydata);
	public List<Object[]> createShowChart(String bankname);
	public List<List<Object[]>> createChartReport(String bankname);
}
