package com.cias.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cias.entity.AppMessages;
import com.cias.entity.ApplicationLabel;
import com.cias.entity.AuditHistory;
import com.cias.entity.Banks;
import com.cias.entity.ColumnNames;
import com.cias.entity.QueryData;
import com.cias.entity.RequiredField;
import com.cias.entity.TableMetaData;
import com.cias.entity.TellerMaster;
import com.cias.entity.ViewInfo;
import com.cias.service.ApplicationLabelService;
import com.cias.service.GenerateCheckDigitService;
import com.cias.utility.ApplicationLabelCache;
import com.cias.utility.CebiConstant;
import com.google.gson.Gson;

@Repository
@Transactional
public class AdminReportDaoImpl implements AdminReportDao {

	@Autowired
	ApplicationLabelService applicationLabelService;

	@Autowired
	ApplicationLabelDao applicationLabelDao;

	@Autowired
	GenerateCheckDigitService generateCheckDigitService;

	@Autowired
	CebiConstant cebiConstant;

	@Autowired
	SessionFactory sessionFactory;

	private static final Logger logger = Logger.getLogger(AdminReportDaoImpl.class);

	public String populateQuery(QueryData table, String parameter, String criteria) {
		
		if(criteria.trim().length()>0){
			criteria = criteria + " AND "+table.getTable1()+".RUN_DATE="+"'"+table.getOndate()+"'"+" ";
			}else if (criteria.trim().length()==0 && table.getOndate().trim().length()>0){
				
				criteria = table.getTable1()+".RUN_DATE="+"'"+table.getOndate()+"'"+" ";
			}
		
		
		String sql = "SELECT";
		String parameterS = "  ";
		if (parameter.trim().length() > 0) {
			parameterS += parameter.substring(0, (parameter.length() - 1)) + " ";
		} else {
			parameterS += " * ";
		}

		if (criteria.trim().length() > 0 && parameter.contains("(")&& parameter.contains(")") || (criteria.trim().length() > 0 )) { 
			if (criteria.trim().length() > 0 && parameter.contains("(") && parameter.contains(")")) {
				
				sql = sql + parameterS + CebiConstant.QRY_FROM + table.getTable1() + CebiConstant.QRY_WHERE;
				sql += criteria;
				
			} else  {
				sql = sql + parameterS + CebiConstant.QRY_FROM + table.getTable1() + CebiConstant.QRY_WHERE;
				sql += criteria + CebiConstant.FETCH100;
			}
			
		} else if(parameter.contains("(")&& parameter.contains(")")) {

			sql = sql + parameterS + " from " + table.getTable1() +" ";
			
		}else {
			sql = sql + parameterS + " from " + table.getTable1() + CebiConstant.FETCH100;
		}
		if (table.getGroupby() != null && table.getGroupby().trim().length() > 0) {
			String groups = table.getGroupby().substring(0, (table.getGroupby().length() - 1));
			sql = sql + "GROUP BY " + groups + CebiConstant.FETCH100;
		}
		return sql;
	}
	
	
	public String populateJoinQuery(QueryData table, String parameter, String criteria){
		if(criteria.trim().length()>0){
			criteria = criteria + " AND "+table.getTable1()+".RUN_DATE="+"'"+table.getOndate()+"'"+" AND "+table.getTable2()+".RUN_DATE="+"'"+table.getOndate()+"'"+" ";
			}else if (criteria.trim().length()==0 && table.getOndate().trim().length()>0){
				
				criteria = table.getTable1()+".RUN_DATE="+"'"+table.getOndate()+"'"+" AND "+table.getTable2()+".RUN_DATE="+"'"+table.getOndate()+"'"+" ";
			}
		String sql = "SELECT";
		String parameterS = "  ";
		if (parameter.trim().length() > 0) {
			parameterS += parameter.substring(0, (parameter.length() - 1)) + " ";
		} else {
			parameterS += " * ";
		}
		  String join = table.getJoinFilter();
		  String joinType = table.getJoinType();
			 if(join.trim().contains(table.getTable1()) && join.contains(table.getTable2()) || join!=null){
				
				 if(criteria.trim().length() > 0 && (parameter.contains("(")&& parameter.contains(")")) || (criteria.trim().length() > 0 )) {
					 
					 if (criteria.trim().length() > 0 && parameter.contains("(") && parameter.contains(")")) {
							
						 sql=sql+ parameterS + CebiConstant.QRY_FROM + table.getTable1() +" "+joinType+" "+table.getTable2()+ " ON " + join.trim()+" "+CebiConstant.QRY_WHERE + criteria;
							
						} else  {
							sql=sql+ parameterS + CebiConstant.QRY_FROM + table.getTable1() +" "+joinType+" "+table.getTable2()+ " ON " + join.trim()+" "+CebiConstant.QRY_WHERE + criteria+" "+CebiConstant.FETCH100;
						}
					 
				} else if(parameter.contains("(")&& parameter.contains(")")) {
				sql = sql + parameterS + CebiConstant.QRY_FROM + table.getTable1() +" "+joinType+" "+table.getTable2()+ " ON " + join.trim()+ " ";
				}else {
					sql=sql+ parameterS + CebiConstant.QRY_FROM + table.getTable1() + " "+ joinType+" "+table.getTable2()+ " ON " + join.trim()+" "+CebiConstant.FETCH100;
				}
			}

		 if (table.getGroupby() != null && table.getGroupby().trim().length() > 0) {
				String groups = table.getGroupby().substring(0, (table.getGroupby().length() - 1));
				sql = sql + "GROUP BY " + groups + CebiConstant.FETCH100;
			}
		return sql;
	}
	
	// not using present
	/*@SuppressWarnings("unchecked")
	public List<String> joinsList(){
		logger.info("retrieveAllLabels start time:: " + System.currentTimeMillis());
		List<String> joins = null;
		Map<String, List<String>> cache = ApplicationLabelCache.getJoinViewInstance();
		
		if(cache.get("Joins")==null){
			Session session = sessionFactory.getCurrentSession();
			joins = (List<String>) session.createSQLQuery("select * from table_joins").setCacheable(true).list();
			cache.put("Joins", joins);
		} else {
			for (Map.Entry<String, List<String>> entry : cache.entrySet()) {
				joins = (List<String>) entry.getValue();
			}
		}
		return joins;
	}*/
	
	public List<TableMetaData> populateDataTable(QueryData getTableData, String bank, TellerMaster master) {
		String parameter = "";
		String criteria = "";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		Session session = null;
		String query = null;
		ColumnNames field;
		
		
		
		List<ColumnNames> names = new ArrayList<>();
		List<AppMessages> appMessages = new ArrayList<>();
		List<TableMetaData> data = new ArrayList<>();
		List<Map<String, Object>> MultiChart = new ArrayList<Map<String, Object>>();
		List<String> MultiChartfields = new ArrayList<String>();
		
		TableMetaData tableMetaData = new TableMetaData();
		
		List<ApplicationLabel> labels = null;

		try {
			session = cebiConstant.getCurrentSession(bank);
    		connection = ((SessionImpl) session).connection();
			parameter = getTableData.getParameter().trim().length() > 0 ? getTableData.getParameter() : "";
			criteria = getTableData.getQuery().trim().length() > 0 ? getTableData.getQuery() : "";
			if(getTableData.getTable2()=="" || getTableData.getTable2()==null){
				query = populateQuery(getTableData, parameter, criteria);	
			}else{
				query = populateJoinQuery(getTableData, parameter, criteria);	
			}
			
			populateAuditHistory(getTableData.getTable1(),getTableData.getTable2(), master, query);
			if (criteria != null && !criteria.isEmpty()) {
				validateTableCriteria(criteria, getTableData, tableMetaData, appMessages);
			}
			if (tableMetaData.getAppMessage() == null || tableMetaData.getAppMessage().size() == 0) {
				logger.info("Query Generated During get Table Data populateDataTable ():: " + query);
				prepareStatement = connection.prepareStatement(query);
				resultSet = prepareStatement.executeQuery();
				String lstparam = parameter.substring(0, (parameter.length() - 1));
				List<String> dbColumns = Arrays.asList(lstparam.split(","));
				labels = applicationLabelService.retrieveAllLabels();
				while (resultSet.next()) {
					tableMetaData = new TableMetaData();
					
					for (String label : dbColumns) {
						String labeloriginal=null;
						String ApplabelCode =null;
						if (label.contains("(") && label.contains(")")){ 
							
							  ApplabelCode = label.substring(label.indexOf('(') + 1, label.indexOf(')'));
							  labeloriginal =label.substring(label.indexOf("As")+3, label.length());
							  label=labeloriginal;
							  
						}else{
							   ApplabelCode = label.contains("AS") && !label.contains(")") ? label.substring(0, label.indexOf("AS")-1) : label;
							   label = label.contains("AS") && !label.contains(")") ? label.substring(label.indexOf("AS")+3, label.length()) : label;
							
						}
						label.trim();
						field = new ColumnNames();
						
						if (resultSet.getString(label) == null || resultSet.getString(label) == "")
							field.setField(" ");
						else
							if(label.toUpperCase().contains("AVG_OF")){
							 field.setField(String.format("%.2f",Double.parseDouble(resultSet.getString(label))));
						   }else{
							   field.setField(resultSet.getString(label));
						   }
						
						field.setName(addApplicationLabels(ApplabelCode, labels, labeloriginal));
						names.add(field);
						
				  	    Map<String, Object> map =null;
						
						if (label.toUpperCase().contains("BRANCH_NO") || label.toUpperCase().contains("BRANCH") || label.toUpperCase().contains("BR_NO")) {
						    map = new HashMap<String, Object>();
							map.put("Branch_No",resultSet.getString(label));
							MultiChartfields.add("Branch_No");
						}
						
						if ((label.toUpperCase().contains("SUM_OF")||label.toUpperCase().contains("AVG_OF")||label.toUpperCase().contains("MAX_OF")||label.toUpperCase().contains("MIN_OF"))){
						    map = new HashMap<String, Object>();
						    if (resultSet.getString(label)!=null){
						    	map.put(addApplicationLabels(ApplabelCode, labels , labeloriginal),Double.parseDouble(resultSet.getString(label)));
								MultiChartfields.add(addApplicationLabels(ApplabelCode, labels , labeloriginal));
						      }
					       }
						
						if (map!=null) {
							MultiChart.add(map);
						}
						
					}
					 tableMetaData.setNames(names);
				}
			}
			

			Gson gson = new Gson();
			tableMetaData.setChartsData(gson.toJson(MultiChart));
			tableMetaData.setChartsDataFields(MultiChartfields);
			tableMetaData.setAppLabels(labels);
			data.add(tableMetaData);
			validateTableData(data, appMessages);
			
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) { /* ignored */
				}
			}
			if (prepareStatement != null) {
				try {
					prepareStatement.close();
				} catch (SQLException e) { /* ignored */
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) { /* ignored */
				}
			}
		}
		return data;

	}


	protected void populateAuditHistory(String table1,String table2, TellerMaster master, String query) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		AuditHistory audit = new AuditHistory();
		audit.setTellerId(Integer.parseInt(master.getTellerid()));
		audit.setBranchId(master.getBranchid());
		audit.setBankCode(master.getBankCode());
		audit.setQuery(query);
		audit.setTable1(table1);
		audit.setTable2(table2);
		audit.setAudDate(simpleDateFormat.format(new Date()));
		sessionFactory.getCurrentSession().save(audit);
	}

	protected void validateTableCriteria(String criteria, QueryData getTableData, TableMetaData tableMetaData, List<AppMessages> appMessages) {
		if ((!getTableData.getTable1().isEmpty() || getTableData.getTable1()!=null) && (getTableData.getTable2().isEmpty())){
			addValidationError(criteria, tableMetaData, appMessages, populateFields(getTableData.getTable1()));
		}else if ((!getTableData.getTable2().isEmpty() || getTableData.getTable2()!=null) && (!getTableData.getTable1().isEmpty())){
			addValidationErrorJoin(criteria, tableMetaData, appMessages, populateFields(getTableData.getTable1()),populateFields(getTableData.getTable2()));
		}

	}

	@SuppressWarnings("unchecked")
	public RequiredField populateFields(String table) {
		List<RequiredField> field = new ArrayList<>();
		RequiredField requiredField = new RequiredField();
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("FROM RequiredField R WHERE R.tabel=:tbl_name");
		query.setParameter("tbl_name", table);
		field = (List<RequiredField>) query.list();
		if (field.size() >= 1)
			requiredField = field.get(0);
		return requiredField;
	}

	private void addValidationError(String criteriaList, TableMetaData tableMetaData, List<AppMessages> appMessages, RequiredField required) {
		if (required.getFilterfield() != null) {
			String mandate = required.getFilterfield();
			for (String str : mandate.split(",")) {
				if (criteriaList.contains(str.trim())) {
					continue;
				}
				appMessages.add(new AppMessages(str, str.toLowerCase() + " is Required"));
			}

			tableMetaData.setAppMessage(appMessages);
		}
	}
	
	private void addValidationErrorJoin(String criteriaList, TableMetaData tableMetaData, List<AppMessages> appMessages, RequiredField required,RequiredField requiredJoin) {
		
		
		if (criteriaList.contains(required.getTabel()) && !criteriaList.contains(requiredJoin.getTabel())){

			if (required.getFilterfield() != null) {
				
				String mandate = required.getFilterfield();
				for (String str : mandate.split(",")) {
					if (criteriaList.contains(str.trim())) {
						continue;
					}
					appMessages.add(new AppMessages(str, str.toLowerCase() + " is Required"));
				}

				tableMetaData.setAppMessage(appMessages);
			}
		}else if (criteriaList.contains(requiredJoin.getTabel()) && !criteriaList.contains(required.getTabel())){
			
	             if (requiredJoin.getFilterfield() != null) {
				
				String mandate = requiredJoin.getFilterfield();
				for (String str : mandate.split(",")) {
					if (criteriaList.contains(str)) {
						continue;
					}
					appMessages.add(new AppMessages(str, str.toLowerCase() + " is Required"));
				}

				tableMetaData.setAppMessage(appMessages);
			}
		} else{
			
		     if (requiredJoin.getFilterfield() != null && required.getFilterfield() != null) {
				
					String mandate = requiredJoin.getFilterfield().concat(",").concat(required.getFilterfield());
					
					for (String str : mandate.split(",")) {
						if (criteriaList.contains(str)) {
							continue;
						}
						appMessages.add(new AppMessages(str, str.toLowerCase() + " is Required"));
					}

					tableMetaData.setAppMessage(appMessages);
				}
			
		}

	}

	protected String addApplicationLabels(String label, List<ApplicationLabel> labels,String labeloriginal){
		for (ApplicationLabel lbl : labels) {
			if (lbl.getLabelCode().equalsIgnoreCase(label)) {
				
				if ((labeloriginal!=null)){
					//labeloriginal.contains("(") && labeloriginal.contains(")") // && (label.length())> labeloriginal.length()
					 label=labeloriginal;
				}else{
					
					label = lbl.getAppLabel();
				}
				//label = lbl.getAppLabel();
				break;
			}
		}
		return label;
	}

	private void validateTableData(List<TableMetaData> data, List<AppMessages> appMessages) {
		for (TableMetaData tableMetaData : data) {
			// tableMetaData.setAppLabels(applicationLabelDao.retrieveAllLabels());
			if (tableMetaData.getNames() == null || tableMetaData.getNames().isEmpty()) {
				appMessages.add(new AppMessages("NO_RESULTS", CebiConstant.NO_RESULT));
				tableMetaData.setAppMessage(appMessages);
			}
		}
	}

	public void saveFavouriteQuery(QueryData data) {
		String parameter = "";
		String criteria = "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		parameter = data.getParameter().trim().length() > 0 ? data.getParameter() : "";
		criteria = data.getQuery().trim().length() > 0 ? data.getQuery() : "";
		String query = populateQuery(data, parameter, criteria);
		data.setFinalQry(query);
		data.setCurrentDate(simpleDateFormat.format(new Date()));
		sessionFactory.getCurrentSession().save(data);
	}

	@Override
	public List<QueryData> retrieveFavouriteList(String bankCode) {
		List<QueryData> lists = new ArrayList<QueryData>();
		Query query = sessionFactory.getCurrentSession().createQuery("FROM QueryData qd WHERE qd.bankCode=:code");
		query.setParameter("code", bankCode);
		lists = (List<QueryData>) query.list();
		return lists;

	}

	@Override
	public List<QueryData> getReportDetails(int id) {
		List<QueryData> lists = new ArrayList<>();
		Query query = sessionFactory.getCurrentSession().createQuery("FROM QueryData qd WHERE qd.id=:id");
		query.setParameter("id", id);
		lists = (List<QueryData>) query.list();
		return lists;

	}

	@Override
	public Banks retreiveBankNames() {
		List<Banks> banks = new ArrayList<>();
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Banks ORDER BY bankName");
		banks = (List<Banks>) query.list();
		Banks bank= banks.get(1);
		return  bank;
	}

	@Override
	public Banks retreiveDbConnection(String bank) {
		List<Banks> banks = new ArrayList<>();
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Banks WHERE bankCode = :code");
		query.setParameter("code", bank);
		banks = (List<Banks>)query.list();
		return banks.get(0);
	}

	@Override
	public String populateBankType(String bankcode) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery("SELECT bank_type FROM cesys004 WHERE bank_code=:code");
		query.setParameter("code", bankcode);
		String type = (String) query.list().get(0);
		return type;
	}

	@Override
	public List<ViewInfo> retreiveViewDetails(String type) {
		List<ViewInfo> viewInfos = new ArrayList<>();
		Query query = null;
		if (type != null && type.equalsIgnoreCase("NB")) {
			query = sessionFactory.getCurrentSession().createQuery("FROM ViewInfo WHERE access NOT IN (2) AND STATUS NOT IN('deactive')");
		} else {
			query = sessionFactory.getCurrentSession().createQuery("FROM ViewInfo WHERE access NOT IN (0) AND STATUS NOT IN('deactive')");
		}
		viewInfos = (List<ViewInfo>) query.list();
		viewInfos.get(0).setAppLabels(applicationLabelDao.retrieveAllLabels());
		return viewInfos;
	}
	
	
	@Override
	public boolean deleteviews(QueryData qrydata) {
			int result=0;
			Query query = sessionFactory.getCurrentSession().createSQLQuery("delete from cesys003 WHERE id in('"+qrydata.getParameter()+"')");
			result=query.executeUpdate();
			if(result>0){
		    	return true;
		} else {
			return false;
		}
	}
	@SuppressWarnings("unchecked")
	public List<Object[]> createShowChart(String bank) {
        logger.info("Inside populateConsolidatedata start time ::" + System.currentTimeMillis());
        Session session = cebiConstant.getCurrentSession(bank);
       //previous query select to_char(tdp_date,'Mon') as date,extract(year from CURRENT_DATE) as year,ROUND(cast(SUM(tdp_amount)/1000000000 as numeric),2) as amount from public.trn_dash_pnl where tdp_date >= date_trunc('month', now()) - interval '4 month' and tdp_date < date_trunc('month', now()) AND tdp_pnl_code=8 GROUP BY date,DATE_PART('MONTH', tdp_date) ORDER BY DATE_PART('MONTH', tdp_date)
        Query query = session.createSQLQuery("select ROUND(cast(SUM(transaction_amount)/100000000 as numeric),2) as amount,to_char(transaction_date,'Mon') as date from public.loan_trasaction group by date,DATE_PART('MONTH', transaction_date) ORDER BY DATE_PART('MONTH', transaction_date)");
        List<Object[]> list = query.list();
        return list;

}
	//landing charts
	@SuppressWarnings("unchecked")
	@Override
	public List<List<Object[]>>  createChartReport(String bankname) {
	 
	logger.info("Inside populateConsolidatedata start time ::" + System.currentTimeMillis());
	Session session = cebiConstant.getCurrentSession(bankname);
	ArrayList<String> list=new ArrayList<String>();  
	        list.add(" with dep as ( (select branch_no,sum(currbal) as Balance from ccod_master where currbal > 0 group by branch_no union all select branch_no,sum(curr_bal) as Balance from demand_dep_master where curr_bal > 0 group by branch_no union all select branch_no,sum(principal) as Balance from time_dep_master where principal > 0 group by branch_no union all select branch_no,sum(curr_bal) as Balance from demand_dep_master where curr_bal > 0 group by branch_no ) ) select branch_no , round(CAST(sum(Balance)/1000000 AS NUMERIC),2) from dep group by branch_no order by branch_no");  
	        list.add("with Adv as ( (select br_no as branch_no, sum(balance) as Balance from Loan_Details group by br_no union all select branch_no as branch_no, sum(currbal) as Balance from ccod_master where currbal < 0 group by branch_no) ) select branch_no , round(CAST(sum(Balance)/1000000 AS NUMERIC),2) from Adv group by branch_no order by branch_no");  
	        list.add("with Npa as ( (select branch_no,sum(currbal) as Balance from ccod_master where currbal > 0 and npa_ind >= 4 group by branch_no union all select br_no as branch_no, sum(balance) as Balance from Loan_Details where npa_ind >= 4 group by br_no )) select branch_no , round(CAST(sum(Balance)/1000000 AS NUMERIC),2) from Npa group by branch_no order by branch_no ;");  
	        List<Object[]> listdata=null;
	        List<List<Object[]>> listdataOne=new ArrayList<List<Object[]>>();
	        for(String s:list) {  
	        Query query = session.createSQLQuery(s);
	         listdata = new ArrayList<Object[]>();
	         listdata = query.list();
	         listdataOne.add(listdata);
	        } 
	        return listdataOne;
	}
	 

	
}
