package com.cias.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Ccdp010Id generated by hbm2java
 */

public class Branchreport implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String brcdBankCd;
	private String reportDate;
	private String branchCode;
	private String acctNo;

	public Branchreport() {
	}

	public Branchreport(String brcdBankCd, String reportDate, String branchCode, String acctNo) {
		this.brcdBankCd = brcdBankCd;
		this.reportDate = reportDate;
		this.branchCode = branchCode;
		this.acctNo = acctNo;
	}

	@Column(name = "BRCD_BANK_CD", nullable = false, length = 5)
	public String getBrcdBankCd() {
		return this.brcdBankCd;
	}

	public void setBrcdBankCd(String brcdBankCd) {
		this.brcdBankCd = brcdBankCd;
	}

	@Column(name = "REPORT_DATE", nullable = false, length = 10)
	public String getReportDate() {
		return this.reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	@Column(name = "BRANCH_CODE", nullable = false, length = 5)
	public String getBranchCode() {
		return this.branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	@Column(name = "ACCT_NO", nullable = false, length = 17)
	public String getAcctNo() {
		return this.acctNo;
	}

	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}

	

}