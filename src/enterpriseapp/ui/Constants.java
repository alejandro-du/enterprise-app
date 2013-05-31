package enterpriseapp.ui;

import enterpriseapp.Utils;

public abstract class Constants {
	
	public static final boolean dbUseCloudFoundryDatabase = new Boolean(Utils.getProperty("db.useCloudFoundryDatabase", "false"));
	public static final String dbPersistenceUnit() { return Utils.getProperty("db.persistenceUnit"); }
	public static final String dbDriver() { return Utils.getProperty("db.driver"); }
	public static final String dbUrl() { return Utils.getProperty("db.url"); }
	public static final String dbUser() { return Utils.getProperty("db.user"); }
	public static final String dbPassword() { return Utils.getProperty("db.password"); }
	public static final String dbDialect() { return Utils.getProperty("db.dialect"); }
	public static final String dbSchemaGeneration() { return Utils.getProperty("db.schemaGeneration"); }
	public static final String dbShowSQL() { return Utils.getProperty("db.show_sql"); }
	public static final String dbMinSize() { return Utils.getProperty("db.pool.min_size"); }
	public static final String dbMaxSize() { return Utils.getProperty("db.pool.max_size"); }
	public static final String dbPoolTimeout() { return Utils.getProperty("db.pool.timeout"); }
	public static final String dbMaxStatements() { return Utils.getProperty("db.pool.max_statements"); }
	public static final String dbPoolValidationQuery() { return Utils.getProperty("db.pool.validationQuery"); }
	public static final String dbInterceptor() { return Utils.getProperty("db.interceptor"); }
	public static final String dbMappingFiles() { return Utils.getProperty("db.mappingFiles"); }
	
	public static final String mailSmtpHost = Utils.getProperty("mail.smtp.host");
	public static final String mailSmtpPort = Utils.getProperty("mail.smtp.port");
	public static final String mailSmtpAddress = Utils.getProperty("mail.smtp.address");
	public static final String mailSmtpUsername = Utils.getProperty("mail.smtp.username");
	public static final String mailSmtpPassword() { return Utils.getProperty("mail.smtp.password"); }
	public static final String mailDeviateTo = Utils.getProperty("mail.deviateTo");
	
	public static final int appSessionTimeout = new Integer(Utils.getProperty("app.sessionTimeout"));
	public static final boolean appCollectLogFiles = new Boolean(Utils.getProperty("app.collectLogFiles"));
	public static final String appLogBasedAuditFormat = Utils.getProperty("app.logBasedAuditFormat");
	
	public static final String reportPageWidth = Utils.getProperty("report.pageWidth", "215.9");
	public static final String reportPageHeight = Utils.getProperty("report.pageHeight", "279.4");
	public static final String reportMarginTop = Utils.getProperty("report.marginTop", "15");
	public static final String reportMarginBottom = Utils.getProperty("report.marginBottom", "15");
	public static final String reportMarginLeft = Utils.getProperty("report.marginLeft", "15");
	public static final String reportMarginRight = Utils.getProperty("report.marginRight", "15");
	
	public static final String uiYes = Utils.getProperty("ui.yes");
	public static final String uiNo = Utils.getProperty("ui.no");
	public static final String uiRequiredField = Utils.getProperty("ui.requiredField");
	public static final String uiNew = Utils.getProperty("ui.new");
	public static final String uiModify = Utils.getProperty("ui.modify");
	public static final String uiDelete = Utils.getProperty("ui.delete");
	public static final String uiSave = Utils.getProperty("ui.save");
	public static final String uiCreate = Utils.getProperty("ui.create");
	public static final String uiCancel = Utils.getProperty("ui.cancel");
	public static final String uiFirst = Utils.getProperty("ui.first");
	public static final String uiPrevious = Utils.getProperty("ui.previous");
	public static final String uiNext = Utils.getProperty("ui.next");
	public static final String uiLast = Utils.getProperty("ui.last");
	public static final String uiAdd = Utils.getProperty("ui.add");
	public static final String uiSaved = Utils.getProperty("ui.saved");
	public static final String uiDeleted = Utils.getProperty("ui.deleted");
	public static final String uiUnknownUser = Utils.getProperty("ui.unknownUser");
	public static final String uiUnknownIp = Utils.getProperty("ui.unknownIp");
	public static final String uiDownloadFile = Utils.getProperty("ui.downloadFile");
	public static final String uiUploadFile = Utils.getProperty("ui.uploadFile");
	public static final String uiPleaseConfirm = Utils.getProperty("ui.pleaseConfirm");
	public static final String uiConfirmDeletion = Utils.getProperty("ui.confirmDeletion");
	public static final String uiConfirmClose = Utils.getProperty("ui.confirmClose");
	public static final String uiMatchesFound = Utils.getProperty("ui.matchesFound");
	public static final String uiMatches = Utils.getProperty("ui.matches");
	public static final String uiStarting = Utils.getProperty("ui.starting");
	public static final String uiEnding = Utils.getProperty("ui.ending");
	public static final String uiExport = Utils.getProperty("ui.export");
	public static final String uiExportToExcel = Utils.getProperty("ui.exportToExcel");
	public static final String uiShowCount = Utils.getProperty("ui.showCount");
	public static final String uiImportFromClipboard = Utils.getProperty("ui.importFromClipboard");
	public static final String uiRefresh = Utils.getProperty("ui.refresh");
	public static final String uiImportFromClipboardInstructions(String columns) { return String.format(Utils.getProperty("ui.importFromClipboardInstructions"), columns); }
	public static final String uiCaseSensitive = Utils.getProperty("ui.caseSensitive");
	public static final String uiOnlyMatchPrefix = Utils.getProperty("ui.onlyMatchPrefix");
	public static final String uiPdf = Utils.getProperty("ui.pdf");
	public static final String uiExcel = Utils.getProperty("ui.excel");
	public static final String uiWord = Utils.getProperty("ui.word");
	public static final String uiPowerPoint = Utils.getProperty("ui.powerPoint");
	public static final String uiOdt = Utils.getProperty("ui.odt");
	public static final String uiOds = Utils.getProperty("ui.ods");
	public static final String uiRtf = Utils.getProperty("ui.rtf");
	public static final String uiHtml = Utils.getProperty("ui.html");
	public static final String uiCsv = Utils.getProperty("ui.csv");
	public static final String uiXml = Utils.getProperty("ui.xml");
	public static final String uiConfiguration = Utils.getProperty("ui.configuration");
	public static final String uiPrintBackgroundOnOddRows = Utils.getProperty("ui.printBackgroundOnOddRows");
	public static final String uiPrintColumnNames = Utils.getProperty("ui.printColumnNames");
	public static final String uiStretchWithOverflow = Utils.getProperty("ui.stretchWithOverflow");
	public static final String uiColumns = Utils.getProperty("ui.columns");
	public static final String uiGrouping = Utils.getProperty("ui.grouping");
	public static final String uiColumnsPerPage = Utils.getProperty("ui.columnsPerPage");
	public static final String uiPageWidth = Utils.getProperty("ui.pageWidth");
	public static final String uiPageHeight = Utils.getProperty("ui.pageHeight");
	public static final String uiMarginTop = Utils.getProperty("ui.marginTop");
	public static final String uiMarginBottom = Utils.getProperty("ui.marginBottom");
	public static final String uiMarginLeft = Utils.getProperty("ui.marginLeft");
	public static final String uiMarginRight = Utils.getProperty("ui.marginRight");
	public static final String uiParameters = Utils.getProperty("ui.parameters");
	public static final String uiObservations = Utils.getProperty("ui.observations");
	public static final String uiSeeObservationsOnTheReport = Utils.getProperty("ui.seeObservationsOnTheReport");
	public static final String uiReverse = Utils.getProperty("ui.reverse");
	public static final String uiEmptyReport = Utils.getProperty("ui.emptyReport");
	public static final String uiWindows = Utils.getProperty("ui.windows");
	public static final String uiTabs = Utils.getProperty("ui.tabs");
	public static final String uiCloseAll = Utils.getProperty("ui.closeAll");
	public static final String uiName = Utils.getProperty("ui.name");
	public static final String uiSize = Utils.getProperty("ui.size");
	public static final String uiLastUpdate = Utils.getProperty("ui.lastUpdate");
	public static final String uiHideFilter = Utils.getProperty("ui.hideFilter");
	public static final String uiShowFilter = Utils.getProperty("ui.showFilter");
	public static final String uiHqlQuery = Utils.getProperty("ui.hqlQuery");
	public static final String uiExecute = Utils.getProperty("ui.execute");
	public static final String uiClear = Utils.getProperty("ui.clear");
	public static final String uiMaxResults = Utils.getProperty("ui.maxResults");
	
	public static final String uiError = Utils.getProperty("ui.error");
	public static final String uiImportFailed = Utils.getProperty("ui.importFailed");
	public static final String uiImportFailedWrongColumnCount = Utils.getProperty("ui.importFailedWrongColumnCount");
	public static final String uiConstraintViolationErrorOnSave = Utils.getProperty("ui.constraintViolationErrorOnSave");
	public static final String uiConstraintViolationErrorOnDelete = Utils.getProperty("ui.constraintViolationErrorOnDelete");
	public static final String uiInvalidEmail = Utils.getProperty("ui.invalidEmail");
	public static final String uiInvalidDoubleValue = Utils.getProperty("ui.invalidDoubleValue");
	public static final String uiInvalidIntegerValue = Utils.getProperty("ui.invalidIntegerValue");
	public static final String uiInvalidLongValue = Utils.getProperty("ui.invalidLongValue");
	public static final String uiInvalidHqlParameterType = Utils.getProperty("ui.invalidHqlParameterType");
	public static final String uiReportConfigurationError = Utils.getProperty("ui.reportConfigurationError");
	public static final String uiMaxLengthExceeded(int length) { return Utils.getProperty("ui.maxLengthExceeded", new String[] {"" + length}); }
	
	public static final String uiCommunicationErrorMessage = Utils.getProperty("ui.communicationErrorMessage");
	public static final String uiInternalErrorMessage = Utils.getProperty("ui.internalErrorMessage");
	public static final String uiCookiesDisabledMessage = Utils.getProperty("ui.cookiesDisabledMessage");
	public static final String uiOutOfSyncMessage = Utils.getProperty("ui.outOfSyncMessage");
	public static final String uiTerminalErrorMessage = Utils.getProperty("ui.terminalErrorMessage");
	public static final String uiErrorTime = Utils.getProperty("ui.errorTime");
	
}
