/***********************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 ***********************************************************************/

package org.eclipse.birt.chart.model.data.impl;

import org.eclipse.birt.chart.model.data.BubbleDataSet;
import org.eclipse.birt.chart.model.data.DataPackage;
import org.eclipse.birt.chart.model.data.DataSet;
import org.eclipse.birt.chart.model.data.DateTimeDataSet;
import org.eclipse.birt.chart.model.data.DifferenceDataSet;
import org.eclipse.birt.chart.model.data.GanttDataSet;
import org.eclipse.birt.chart.model.data.NullDataSet;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.StockDataSet;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Set</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.birt.chart.model.data.impl.DataSetImpl#getValues <em>Values</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DataSetImpl extends EObjectImpl implements DataSet
{

	/**
	 * The default value of the '{@link #getValues() <em>Values</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getValues()
	 * @generated
	 * @ordered
	 */
	protected static final Object VALUES_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValues() <em>Values</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getValues()
	 * @generated
	 * @ordered
	 */
	protected Object values = VALUES_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected DataSetImpl( )
	{
		super( );
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass( )
	{
		return DataPackage.Literals.DATA_SET;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Object getValues( )
	{
		return values;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setValues( Object newValues )
	{
		Object oldValues = values;
		values = newValues;
		if ( eNotificationRequired( ) )
			eNotify( new ENotificationImpl( this,
					Notification.SET,
					DataPackage.DATA_SET__VALUES,
					oldValues,
					values ) );
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet( int featureID, boolean resolve, boolean coreType )
	{
		switch ( featureID )
		{
			case DataPackage.DATA_SET__VALUES :
				return getValues( );
		}
		return super.eGet( featureID, resolve, coreType );
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet( int featureID, Object newValue )
	{
		switch ( featureID )
		{
			case DataPackage.DATA_SET__VALUES :
				setValues( newValue );
				return;
		}
		super.eSet( featureID, newValue );
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset( int featureID )
	{
		switch ( featureID )
		{
			case DataPackage.DATA_SET__VALUES :
				setValues( VALUES_EDEFAULT );
				return;
		}
		super.eUnset( featureID );
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet( int featureID )
	{
		switch ( featureID )
		{
			case DataPackage.DATA_SET__VALUES :
				return VALUES_EDEFAULT == null ? values != null
						: !VALUES_EDEFAULT.equals( values );
		}
		return super.eIsSet( featureID );
	}

	private static DataSet copyInstanceThis( DataSet src )
	{
		if ( src == null )
		{
			return null;
		}

		DataSetImpl dest = new DataSetImpl( );

		dest.values = src.getValues( );

		return dest;
	}

	/**
	 * A convenient method to get an instance copy. This is much faster than the
	 * ECoreUtil.copy().
	 * 
	 * @param src
	 * @return
	 */
	public static DataSet copyInstance( DataSet src )
	{
		if ( src == null )
		{
			return null;
		}

		if ( src instanceof NullDataSet )
		{
			return NullDataSetImpl.copyInstance( (NullDataSet) src );
		}
		else if ( src instanceof DifferenceDataSet )
		{
			return DifferenceDataSetImpl.copyInstance( (DifferenceDataSet) src );
		}
		else if ( src instanceof StockDataSet )
		{
			return StockDataSetImpl.copyInstance( (StockDataSet) src );
		}
		else if ( src instanceof GanttDataSet )
		{
			return GanttDataSetImpl.copyInstance( (GanttDataSet) src );
		}
		else if ( src instanceof BubbleDataSet )
		{
			return BubbleDataSetImpl.copyInstance( (BubbleDataSet) src );
		}
		else if ( src instanceof TextDataSet )
		{
			return TextDataSetImpl.copyInstance( (TextDataSet) src );
		}
		else if ( src instanceof DateTimeDataSet )
		{
			return DateTimeDataSetImpl.copyInstance( (DateTimeDataSet) src );
		}
		else if ( src instanceof NumberDataSet )
		{
			return NumberDataSetImpl.copyInstance( (NumberDataSet) src );
		}
		else
		{
			return copyInstanceThis( src );
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString( )
	{
		if ( eIsProxy( ) )
			return super.toString( );

		StringBuffer result = new StringBuffer( super.toString( ) );
		result.append( " (values: " ); //$NON-NLS-1$
		result.append( values );
		result.append( ')' );
		return result.toString( );
	}

} //DataSetImpl
