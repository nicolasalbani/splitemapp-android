package com.splitemapp.android.screen.balance;

import android.content.Context;

import com.splitemapp.android.R;

public enum ProjectTypeMapper {

	monthly			((short)1, R.string.b_monthly),
	one_time		((short)2, R.string.b_one_time);

	private final short projectTypeId;
	private final int stringId;

	ProjectTypeMapper(short projectTypeId, int stringId){
		this.projectTypeId = projectTypeId;
		this.stringId = stringId;
	}

	public int getStringId() {
		return stringId;
	}
	
	public short getProjectTypeId() {
		return projectTypeId;
	}
	
	public String getString(Context context){
		return context.getResources().getString(getStringId());
	}
	
	public static String getString(Context context, String cod){
		return context.getResources().getString(ProjectTypeMapper.valueOf(cod).getStringId());
	}
	
	public static String getCod(Context context, String string){
		for(ProjectTypeMapper projectTypeMapper:ProjectTypeMapper.values()){
			if(projectTypeMapper.getString(context).equals(string)){
				return projectTypeMapper.toString();
			}
		}
		return null;
	}
}
