package com.timefleeting.app;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class TimeFleetingData {

	private static TimeFleetingData timeFleetingData;
	
	private static DB db;
	public static List<Record> pastRecords;
	public static List<Record> futureRecords;
	
	private boolean overdueSort = true;
	
	private TimeFleetingData(Context context) {
		try {
			db = db.getInstance(context);
			Log.d("TimeFleeting", "Loading database successfully.");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static TimeFleetingData getInstance(Context context) 
			throws IOException {
		if (timeFleetingData == null) {
			timeFleetingData = new TimeFleetingData(context);
			pastRecords = db.loadPastRecords();
			futureRecords = db.loadFutureRecords();
			Log.d("TimeFleeting", "The timeFlletingData is NULL.");
			Log.d("TimeFleeting", "Loading " + 
				String.valueOf(pastRecords.size()) + 
				" PAST records successfully.");
			Log.d("TimeFleeting", "Loading " + 
					String.valueOf(futureRecords.size()) + 
					" FUTURE records successfully.");
		}
		return timeFleetingData;
	}
	
	// return weather the new record is saved successfully
	// if successfully, return the new id
	// else return -1
	public int saveRecord(Record record) {
		boolean isUpdate = false;
		if (record.getId() != -1) {
			isUpdate = true;
		}
		int insertId = db.saveRecord(record);
		if (insertId == -1) {
			return -1;
		}
		record.setId(insertId);
		if (record.getType() == "PAST") {
			if (isUpdate) {
				for (Record r : pastRecords) {
					if (r.getId() == record.getId()) {
						r = record;
						break;
					}
				}
			} else {
				pastRecords.add(record);
			}
		} else if (record.getType() == "FUTURE") {
			if (isUpdate) {
				for (Record r : futureRecords) {
					if (r.getId() == record.getId()) {
						r = record;
						break;
					}
				}
			} else {
				futureRecords.add(record);
			}
		}
		return insertId;
	}
	
	public void setOverdueSortTrue() {
		overdueSort = true;
	}
	
	public void setOverdueSortFalse() {
		overdueSort = false;
	}
	
	// sort the past Records by id
	// this is also the default sort in the database
	public void sortPastRecordById() {
		Collections.sort(pastRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
			}
		});
	}
	
	// sort the future Records by id
	// this is also the default sort in the database
	public void sortFutureRecordById() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if (lhsIsOverdue < 0 && rhsIsOverdue < 0) {
						// both are overdue
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else if (lhsIsOverdue >= 0 && rhsIsOverdue < 0) {
						return -1;
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				} else {
					return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
				}
			}
		});
	}
	
	public void sortFutureRecordByIdReversely() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if (lhsIsOverdue < 0 && rhsIsOverdue < 0) {
						// both are overdue
						return Integer.valueOf(rhs.getId()).compareTo(Integer.valueOf(lhs.getId()));
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else if (lhsIsOverdue >= 0 && rhsIsOverdue < 0) {
						return -1;
					} else {
						return Integer.valueOf(rhs.getId()).compareTo(Integer.valueOf(lhs.getId()));
					}
				} else {
					return Integer.valueOf(rhs.getId()).compareTo(Integer.valueOf(lhs.getId()));
				}
			}
		});
	}
	
	// sort the future records by title
	// if the title is same, sort by create time
	// if the create time is same, sort by remind time
	// if the remind time is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByTitle() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getTitle().equals(rhs.getTitle())) {
							return lhs.getTitle().compareTo(rhs.getTitle());
						} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return lhs.getCreateTime().compareTo(rhs.getCreateTime());
						} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return lhs.getRemindTime().compareTo(rhs.getRemindTime());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getTitle().equals(rhs.getTitle())) {
						return lhs.getTitle().compareTo(rhs.getTitle());
					} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return lhs.getCreateTime().compareTo(rhs.getCreateTime());
					} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return lhs.getRemindTime().compareTo(rhs.getRemindTime());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}
	
	// sort the future records by title reversely
	// if the title is same, sort by create time
	// if the create time is same, sort by remind time
	// if the remind time is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByTitleReversely() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getTitle().equals(rhs.getTitle())) {
							return rhs.getTitle().compareTo(lhs.getTitle());
						} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return lhs.getCreateTime().compareTo(rhs.getCreateTime());
						} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return lhs.getRemindTime().compareTo(rhs.getRemindTime());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getTitle().equals(rhs.getTitle())) {
						return rhs.getTitle().compareTo(lhs.getTitle());
					} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return lhs.getCreateTime().compareTo(rhs.getCreateTime());
					} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return lhs.getRemindTime().compareTo(rhs.getRemindTime());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}
	
	// sort the future records by create time
	// if the create time is same, sort by title
	// if the title is same, sort by remind time
	// if the remind time is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByCreateTime() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return lhs.getCreateTime().compareTo(rhs.getCreateTime());
						} else if (!lhs.getTitle().equals(rhs.getTitle())) {
							return rhs.getTitle().compareTo(lhs.getTitle());
						} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return lhs.getRemindTime().compareTo(rhs.getRemindTime());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return lhs.getCreateTime().compareTo(rhs.getCreateTime());
					} else if (!lhs.getTitle().equals(rhs.getTitle())) {
						return rhs.getTitle().compareTo(lhs.getTitle());
					} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return lhs.getRemindTime().compareTo(rhs.getRemindTime());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}
	
	// sort the future records by create time reversely
	// if the create time is same, sort by title
	// if the title is same, sort by remind time
	// if the remind time is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByCreateTimeReversely() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return rhs.getCreateTime().compareTo(lhs.getCreateTime());
						} else if (!lhs.getTitle().equals(rhs.getTitle())) {
							return rhs.getTitle().compareTo(lhs.getTitle());
						} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return lhs.getRemindTime().compareTo(rhs.getRemindTime());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return rhs.getCreateTime().compareTo(lhs.getCreateTime());
					} else if (!lhs.getTitle().equals(rhs.getTitle())) {
						return rhs.getTitle().compareTo(lhs.getTitle());
					} else if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return lhs.getRemindTime().compareTo(rhs.getRemindTime());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}
	
	// sort the future records by remind time
	// if the remind time is same, sort by create time
	// if the create time is same, sort by title 
	// if the title is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByRemindTime() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return lhs.getRemindTime().compareTo(rhs.getRemindTime());
						} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return rhs.getCreateTime().compareTo(lhs.getCreateTime());
						} else if (!lhs.getTitle().equals(rhs.getTitle())) {
							return lhs.getTitle().compareTo(rhs.getTitle());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return lhs.getRemindTime().compareTo(rhs.getRemindTime());
					} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return rhs.getCreateTime().compareTo(lhs.getCreateTime());
					} else if (!lhs.getTitle().equals(rhs.getTitle())) {
						return lhs.getTitle().compareTo(rhs.getTitle());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}
	
	// sort the future records by remind time
	// if the remind time is same, sort by create time
	// if the create time is same, sort by title 
	// if the title is same, sort by star
	// is the star is same, sort by id
	public void sortFutureRecordByRemindTimeReversely() {
		final String currentTimeString;
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");     
		Date curDate = new Date(System.currentTimeMillis());
		currentTimeString = formatter.format(curDate);
		Collections.sort(futureRecords, new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if (overdueSort) {
					// lhs is negative means the lhs is overdue
					int lhsIsOverdue = lhs.getRemindTime().compareTo(currentTimeString);
					// rhs is negative means the rhs is overdue
					int rhsIsOverdue = rhs.getRemindTime().compareTo(currentTimeString);
					if ((lhsIsOverdue < 0 && rhsIsOverdue < 0) || (lhsIsOverdue >= 0 && rhsIsOverdue >= 0)) {
						// both are overdue or both are not overdue
						if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
							return rhs.getRemindTime().compareTo(lhs.getRemindTime());
						} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
							return rhs.getCreateTime().compareTo(lhs.getCreateTime());
						} else if (!lhs.getTitle().equals(rhs.getTitle())) {
							return lhs.getTitle().compareTo(rhs.getTitle());
						} else if (!lhs.getStar().equals(rhs.getStar())) {
							return lhs.getStar().compareTo(rhs.getStar());
						} else {
							return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
						}
					} else if (lhsIsOverdue < 0 && rhsIsOverdue >= 0) {
						return 1;
					} else {
						// lhsIsOverdue >= 0 && rhsIsOverdue < 0
						return -1;
					}
				} else {
					if (!lhs.getRemindTime().equals(rhs.getRemindTime())) {
						return rhs.getRemindTime().compareTo(lhs.getRemindTime());
					} else if (!lhs.getCreateTime().equals(rhs.getCreateTime())) {
						return rhs.getCreateTime().compareTo(lhs.getCreateTime());
					} else if (!lhs.getTitle().equals(rhs.getTitle())) {
						return lhs.getTitle().compareTo(rhs.getTitle());
					} else if (!lhs.getStar().equals(rhs.getStar())) {
						return lhs.getStar().compareTo(rhs.getStar());
					} else {
						return Integer.valueOf(lhs.getId()).compareTo(Integer.valueOf(rhs.getId()));
					}
				}
			}
		});
	}

}
