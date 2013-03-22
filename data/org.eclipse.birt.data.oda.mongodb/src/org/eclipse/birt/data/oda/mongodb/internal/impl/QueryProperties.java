/*
 *************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation - initial API and implementation
 *  
 *************************************************************************
 */

package org.eclipse.birt.data.oda.mongodb.internal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.birt.data.oda.mongodb.impl.MongoDBDriver;
import org.eclipse.birt.data.oda.mongodb.impl.MongoDBDriver.ReadPreferenceChoice;
import org.eclipse.birt.data.oda.mongodb.internal.impl.MDbMetaData.FieldMetaData;
import org.eclipse.birt.data.oda.mongodb.nls.Messages;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.util.JSON;

/**
 * Represents the properties that define a MongoDB ODA data set query.
 */
public class QueryProperties
{
    private static final String MONGO_PROP_PREFIX = DriverUtil.EMPTY_STRING;    // no prefix

    // query components
    private static final String COLLECTION_NAME_PROP = MONGO_PROP_PREFIX.concat( "collectionName" ); //$NON-NLS-1$
    private static final String QUERY_OPERATION_TYPE_PROP = MONGO_PROP_PREFIX.concat( "operationType" ); //$NON-NLS-1$
    private static final String QUERY_OPERATION_EXPR_PROP = MONGO_PROP_PREFIX.concat( "operationExpr" ); //$NON-NLS-1$
    private static final String SELECTED_FIELDS_PROP = MONGO_PROP_PREFIX.concat( "selectedFields" ); //$NON-NLS-1$
    private static final String FIND_QUERY_EXPR_PROP = MONGO_PROP_PREFIX.concat( "findQueryExpr" ); //$NON-NLS-1$
    private static final String SORT_EXPR_PROP = MONGO_PROP_PREFIX.concat( "sortExpr" ); //$NON-NLS-1$    

    // advanced runtime properties
    private static final String QUERY_READ_PREF_PROP = MONGO_PROP_PREFIX.concat( "queryReadPreference" ); //$NON-NLS-1$
    private static final String RT_META_DATA_SEARCH_LIMIT = MONGO_PROP_PREFIX.concat( "rtMDSearchLimit" ); //$NON-NLS-1$
    private static final String CURSOR_BATCH_SIZE_PROP = MONGO_PROP_PREFIX.concat( "batchSize" ); //$NON-NLS-1$
    private static final String SKIP_NUM_DOCS_PROP = MONGO_PROP_PREFIX.concat( "numSkipDocuments" ); //$NON-NLS-1$
    private static final String AUTO_FLATTENING_PROP = MONGO_PROP_PREFIX.concat( "flattenCollections" ); //$NON-NLS-1$
    private static final String INDEX_HINTS_PROP = MONGO_PROP_PREFIX.concat( "indexHints" ); //$NON-NLS-1$
    private static final String NO_TIMEOUT_PROP = MONGO_PROP_PREFIX.concat( "noTimeOut" ); //$NON-NLS-1$
    private static final String PARTIAL_RESULTS_PROP = MONGO_PROP_PREFIX.concat( "allowsPartialResults" ); //$NON-NLS-1$
    
    public static final int DEFAULT_RUNTIME_METADATA_SEARCH_LIMIT = 10;
    public static final int DEFAULT_CURSOR_BATCH_SIZE = 100;    // TODO - good default value?

    private static final String DOC_ID_FIELD_NAME = QueryModel.DOC_ID_FIELD_NAME;
    private static final String ARRAY_BEGIN_MARKER = "["; //$NON-NLS-1$
    private static final String ARRAY_END_MARKER = "]"; //$NON-NLS-1$

    public enum CommandOperationType
    {
        DYNAMIC_QUERY,
        AGGREGATE,
        MAP_REDUCE,
        RUN_DB_COMMAND;
        
        private CommandOperationType(){}
        
        public static CommandOperationType getType( String operationTypeLiteral )
        {
            if( operationTypeLiteral == null || operationTypeLiteral.isEmpty() )
                return DYNAMIC_QUERY;
            if( operationTypeLiteral.equals( AGGREGATE.displayName() ) || operationTypeLiteral.equals( AGGREGATE.name() ))
                return AGGREGATE;
            if( operationTypeLiteral.equals( MAP_REDUCE.displayName() ) || operationTypeLiteral.equals( MAP_REDUCE.name() ))
                return MAP_REDUCE;
            if( operationTypeLiteral.equals( RUN_DB_COMMAND.displayName() ) || operationTypeLiteral.equals( RUN_DB_COMMAND.name() ))
                return RUN_DB_COMMAND;

            return DYNAMIC_QUERY;  // default
        }

        public String displayName()
        {
            if( this == DYNAMIC_QUERY )
                return DriverUtil.EMPTY_STRING;
            if( this == AGGREGATE )
                return Messages.queryProperties_aggrCmdName;
            if( this == MAP_REDUCE )
                return Messages.queryProperties_mapReduceCmdName;
            if( this == RUN_DB_COMMAND )
                return Messages.queryProperties_dbCmdName;
            return DriverUtil.EMPTY_STRING;
        }        
    }

    private static final Map<String,Object> sm_defaultPropsMap = (new QueryProperties()).getPropertiesMap();

    private Map<String,Object> m_propertiesMap;
    

    public QueryProperties( String collectionName )
    {
        setCollectionName( collectionName );
    }

    QueryProperties( Map<String,Object> propertiesMap )
    {
        setValues( propertiesMap );
    }

    private QueryProperties()
    {
        setDefaultValues();
    }

    public static QueryProperties defaultValues()
    {
        return new QueryProperties( sm_defaultPropsMap );
    }
 
    static QueryProperties copy( QueryProperties fromProps )
    {
        if( fromProps == null )
            return null;    // done; nothing to copy
        return new QueryProperties( fromProps.getPropertiesMap() );
    }

    @SuppressWarnings("unchecked")
    public static QueryProperties deserialize( String serializedContent ) 
        throws OdaException
    {
        if( serializedContent == null || serializedContent.trim().isEmpty() )
            return new QueryProperties( (Map<String,Object>)null );

        Exception caughtEx = null;
        try
        {
            DBObject parsedObj = QueryModel.parseExprToDBObject( serializedContent );
            if( parsedObj instanceof Map<?,?>)
                return new QueryProperties( (Map<String,Object>)parsedObj );
        }
        catch( Exception ex )
        {
            caughtEx = ex;
        }

        // not able to de-serialize
        OdaException odaEx = new OdaException( 
                Messages.bind( Messages.queryProperties_errDeSerializeDBObject, serializedContent ));
        if( caughtEx != null )
            odaEx.initCause( caughtEx );
        throw odaEx;
    }

    public String serialize()
    {
        Map<String, Object> definedPropsMap = copyNonDefaultProperties( getPropertiesMap() );

        // convert property values not serializable by BSON serializer
        externalizePropValues( definedPropsMap );

        return JSON.serialize( definedPropsMap );
    }

    private void setDefaultValues()
    {
        setOperationType( CommandOperationType.DYNAMIC_QUERY );
        setQueryReadPreference( ReadPreferenceChoice.DEFAULT_PREFERENCE );
        setRuntimeMetaDataSearchLimit( DEFAULT_RUNTIME_METADATA_SEARCH_LIMIT );
        setBatchSize( DEFAULT_CURSOR_BATCH_SIZE );
        setNumDocsToSkip( 0 );
        setAutoFlattening( false );
        setNoTimeOut( false );
        setPartialResultsOk( true );
    }

    private static Object getDefaultPropValue( String propKey )
    {
        return sm_defaultPropsMap.get( propKey );
    }

    private void setValues( Map<String,Object> propertiesMap )
    {
        if( propertiesMap == null || propertiesMap.isEmpty() )
            return;     // done; nothing to put

        // override existing values with the specified Map entries
        setInternalProperties( propertiesMap );
    }

    void setNonNullValues( Map<String,Object> propertiesMap )
    {
        Map<String,Object> nonNullPropsMap = copyNonNullProperties( propertiesMap );
        // override existing value with the specified Map entry value
        setInternalProperties( nonNullPropsMap );
    }
    
    private void setInternalProperties( Map<String,Object> propertiesMap )
    {
        getPropertiesMap().putAll( propertiesMap );
        internalizePropValues();        
    }

    private void internalizePropValues()
    {
        // convert prop value to internal format (to minimize conversion for internal processing)
        Object propValue = getPropertiesMap().get( QUERY_READ_PREF_PROP );
        if( propValue instanceof String )
            setQueryReadPreference( (String)propValue );

        propValue = getPropertiesMap().get( QUERY_OPERATION_TYPE_PROP );
        if( propValue instanceof String )
            setOperationType( (String)propValue );
    }

    private static void externalizePropValues( Map<String,Object> propertiesMap )
    {
        // convert property value object not serializable by BSON serializer
        // to its string representation
        Object propValue = propertiesMap.get( QUERY_READ_PREF_PROP );
        if( propValue instanceof ReadPreference )
            propertiesMap.put( QUERY_READ_PREF_PROP, ((ReadPreference) propValue).getName() );
 
        propValue = propertiesMap.get( QUERY_OPERATION_TYPE_PROP );
        if( propValue instanceof CommandOperationType )
            propertiesMap.put( QUERY_OPERATION_TYPE_PROP, propValue.toString() );
    }

    private static Map<String,Object> copyNonNullProperties( Map<String,Object> propertiesMap )
    {
        if( propertiesMap == null || propertiesMap.isEmpty() )
            return Collections.emptyMap();     // done; nothing to copy

        Map<String,Object> propsCopy = new HashMap<String,Object>(propertiesMap.size());
        for( Entry<String, Object> propEntry : propertiesMap.entrySet() )
        {
            Object propValue = propEntry.getValue();
            // copy the specified Map entry value if value is not null
            if( propValue != null )
                propsCopy.put( propEntry.getKey(), propValue );
        }
        return propsCopy;
    }

    private static Map<String,Object> copyNonDefaultProperties( Map<String,Object> propertiesMap )
    {
        if( propertiesMap == null || propertiesMap.isEmpty() )
            return Collections.emptyMap();     // done; nothing to copy

        // copy map entries that contains non-null and non-default values
        Map<String,Object> propsCopy = new HashMap<String,Object>(propertiesMap.size());
        for( Entry<String, Object> propEntry : propertiesMap.entrySet() )
        {
            Object propValue = propEntry.getValue();
            if( propValue == null )
                continue;   // skip null value in entry

            // check if entry is of the default value
            String propKey = propEntry.getKey();
            Object defaultValue = getDefaultPropValue( propKey );
            if( propValue instanceof String )
            {   
                if( ((String)propValue).isEmpty() ) 
                    continue;   // skip empty value in entry
      
                if( defaultValue instanceof Boolean || 
                        defaultValue instanceof Integer ||
                        defaultValue instanceof ReadPreference )
                    defaultValue = defaultValue.toString();
            }
            if( propValue.equals( defaultValue ) )
                continue;   // skip default value in entry
            
            // copy the specified Map entry value
            propsCopy.put( propKey, propValue );
        }  
   
        return propsCopy;
    }
 
    Map<String,Object> getPropertiesMap()
    {
        if( m_propertiesMap == null )
            m_propertiesMap = new HashMap<String,Object>();
        return m_propertiesMap;
    }

    public void setCollectionName( String collectionName )
    {
        getPropertiesMap().put( COLLECTION_NAME_PROP, collectionName );
    }

    public String getCollectionName()
    {
        return getStringPropOrEmptyValue( COLLECTION_NAME_PROP );
    }

    public void setOperationType( String opTypeLiteral )
    {
        setOperationType( CommandOperationType.getType( opTypeLiteral.trim() ) );
    }

    public void setOperationType( CommandOperationType opType )
    {
        getPropertiesMap().put( QUERY_OPERATION_TYPE_PROP, opType );
    }

    public CommandOperationType getOperationType()
    {
        CommandOperationType value = getOperationTypeImpl( getPropertiesMap() );
        if( value == null )
            value = getOperationTypeImpl( sm_defaultPropsMap );     // get default value instead
        return value;
    }
    
    private static CommandOperationType getOperationTypeImpl( Map<String, Object> propMap )
    {
        Object propValue = propMap.get( QUERY_OPERATION_TYPE_PROP );
        if( propValue instanceof CommandOperationType )
            return (CommandOperationType)propValue;
        if( propValue instanceof String )
            return CommandOperationType.getType( ((String)propValue).trim() );
        return null;    // non-recognized data type
    }

    public boolean hasValidCommandOperation()
    {
        return hasAggregateCommand() || 
                hasMapReduceCommand() ||
                hasRunCommand();
    }

    public boolean hasAggregateCommand()
    {
        return hasValidCommand( CommandOperationType.AGGREGATE );
    }

    public boolean hasMapReduceCommand()
    {
        return hasValidCommand( CommandOperationType.MAP_REDUCE );
    }

    public boolean hasRunCommand()
    {
        return hasValidCommand( CommandOperationType.RUN_DB_COMMAND );
    }
    
    private boolean hasValidCommand( CommandOperationType opType )
    {
        if( getOperationType() != opType )
            return false;
        return ! getOperationExpression().isEmpty();        
    }

    public void setOperationExpression( String opExpr )
    {
        getPropertiesMap().put( QUERY_OPERATION_EXPR_PROP, opExpr );
    }

    public String getOperationExpression()
    {
        return getStringPropOrEmptyValue( QUERY_OPERATION_EXPR_PROP );
    }
    
    DBObject getOperationExprAsParsedObject( boolean addArrayMarkers ) throws OdaException
    {
        String cmdOpExprText = getOperationExpression();
        if( cmdOpExprText.isEmpty() )
            return null;

        // add array markers for operation pipeline
        if( addArrayMarkers )
            cmdOpExprText = addArrayMarkers( cmdOpExprText );
        
        return QueryModel.parseExprToDBObject( cmdOpExprText );
    }

    static String addArrayMarkers( String expr )
    {
        if( ! expr.startsWith( ARRAY_BEGIN_MARKER ) && 
                ! expr.endsWith( ARRAY_END_MARKER ) )
        {
            StringBuilder strBldr = new StringBuilder(expr.length()+2);
            strBldr.append( ARRAY_BEGIN_MARKER )
                    .append( expr )
                    .append( ARRAY_END_MARKER );
            return strBldr.toString();
        }

        return expr;
    }

    public void setSelectedFields( List<FieldMetaData> selectedFields )
    {
        List<String> fieldNames = new ArrayList<String>(selectedFields.size());
        for( FieldMetaData fieldMd : selectedFields )
        {
            fieldNames.add( fieldMd.getFullName() );
        }
        
        setSelectedFieldNames( fieldNames );
    }

    private void setSelectedFieldNames( List<String> selectedFieldList )
    {
        getPropertiesMap().put( SELECTED_FIELDS_PROP, selectedFieldList );
    }

    /**
     * Returns the full names of selected fields in an ordered list.
     */
    @SuppressWarnings("unchecked")
    public List<String> getSelectedFieldNames()
    {
        Object propValue = getPropertiesMap().get( SELECTED_FIELDS_PROP );
        if( propValue instanceof List<?> )
            return (List<String>)propValue;

        if( propValue instanceof String )
        {
            DBObject projectionKeys = null;
            try
            {
                projectionKeys = QueryModel.parseExprToDBObject( (String)propValue );
            }
            catch( OdaException ex )
            {
                // log and ignore
                DriverUtil.getLogger().log( Level.INFO, 
                        Messages.bind( "Unable to parse the Selected Fields expression: {0}", propValue ), ex ); //$NON-NLS-1$
                return Collections.emptyList();
            }
            
            // extract the included fields to a list of selected fields
            List<String> selectedFieldNames = new ArrayList<String>(projectionKeys.keySet().size());
            for( String key : projectionKeys.keySet() )
            {
                Object value = projectionKeys.get( key );
                if( value instanceof Integer )
                {
                    if( (Integer)value == 0 )   // field is being excluded
                        continue;
                }
                else if( value instanceof Boolean && (Boolean)value == Boolean.FALSE )   // field is being excluded
                    continue;

                selectedFieldNames.add( key );
            }
            return selectedFieldNames;
        }

        return Collections.emptyList();    // null or non-recognized data type
    }

    DBObject getSelectedFieldsAsProjectionKeys() throws OdaException
    {
        Object propValue = getPropertiesMap().get( SELECTED_FIELDS_PROP );
        if( propValue instanceof List<?> )
        {
            BasicDBObject keys = new BasicDBObject();
            @SuppressWarnings("unchecked")
            List<String> fieldNames = (List<String>) propValue;
            for( String field : fieldNames )
            {
                keys.append( field, 1 );
            }
    
            // explicitly exclude docId field if not in projected list
            if( ! keys.containsField( DOC_ID_FIELD_NAME ) )
                keys.append( DOC_ID_FIELD_NAME, 0 );
            return keys;
        }

        if( propValue instanceof String )
        {
            // user-defined projection expression
            return QueryModel.parseExprToDBObject( (String)propValue );
        }

        // non-recognized data type; log and ignore
        if( propValue != null )
            DriverUtil.getLogger().log( Level.INFO, 
                Messages.bind( "Unexpected data type ({0}) in Selected Fields property value.", propValue.getClass().getName() )); //$NON-NLS-1$

        return new BasicDBObject();
    }

    public void setQueryReadPreference( String readPrefLiteral )
    {
        ReadPreference readPref = toReadPreference( readPrefLiteral );
        setQueryReadPreference( readPref );
    }

    public void setQueryReadPreference( ReadPreference readPref )
    {
        getPropertiesMap().put( QUERY_READ_PREF_PROP, readPref );
    }

    public ReadPreference getQueryReadPreference()
    {
        Object propValue = getPropertiesMap().get( QUERY_READ_PREF_PROP );
        if( propValue instanceof String )
            propValue = toReadPreference( ((String)propValue) );
        if( propValue instanceof ReadPreference )
        {            
            if( (ReadPreference)propValue == ReadPreferenceChoice.DEFAULT_PREFERENCE )
                return null;    // let MongoDB server determines default
            return (ReadPreference)propValue;
        }
        return null;    // non-recognized data type; default has null preference
    }

    private static ReadPreference toReadPreference( String readPrefChoiceLiteral )
    {
        return MongoDBDriver.ReadPreferenceChoice.getMongoReadPreference( readPrefChoiceLiteral );
    }

    public void setRuntimeMetaDataSearchLimit( Integer searchLimit )
    {        
        getPropertiesMap().put( RT_META_DATA_SEARCH_LIMIT, searchLimit );
    }

    public Integer getRuntimeMetaDataSearchLimit()
    {
        return getIntPropOrDefaultValue( RT_META_DATA_SEARCH_LIMIT );
    }
    
    public boolean hasRuntimeMetaDataSearchLimit()
    {
         return hasIntPropertyValue( getPropertiesMap(), RT_META_DATA_SEARCH_LIMIT );
    }

    public void setBatchSize( Integer batchSize )
    {
        getPropertiesMap().put( CURSOR_BATCH_SIZE_PROP, batchSize );
    }

    public Integer getBatchSize()
    {
        return getIntPropOrDefaultValue( CURSOR_BATCH_SIZE_PROP );
    }
    
    public boolean hasBatchSize()
    {
        return hasIntPropertyValue( getPropertiesMap(), CURSOR_BATCH_SIZE_PROP );
    }

    public void setNumDocsToSkip( Integer numDocsToSkip )
    {
        getPropertiesMap().put( SKIP_NUM_DOCS_PROP, numDocsToSkip );
    }

    public Integer getNumDocsToSkip()
    {
        return getIntPropOrDefaultValue( SKIP_NUM_DOCS_PROP );
    }
    
    public boolean hasNumDocsToSkip()
    {
        return hasIntPropertyValue( getPropertiesMap(), SKIP_NUM_DOCS_PROP );
    }

    public void setAutoFlattening( Boolean isAutoFlattening )
    {
        getPropertiesMap().put( AUTO_FLATTENING_PROP, isAutoFlattening );
    }

    public boolean isAutoFlattening()
    {
        return getBooleanPropOrDefaultValue( AUTO_FLATTENING_PROP );
    }

    public void setIndexHints( String indexHints )
    {
        getPropertiesMap().put( INDEX_HINTS_PROP, indexHints );
    }

    public String getIndexHints()
    {
        return getStringPropOrEmptyValue( INDEX_HINTS_PROP );
    }
    
    DBObject getIndexHintsAsParsedObject()
    {
        String hintValue = getIndexHints();
        if( hintValue.isEmpty() )
            return null;
        try
        {
            return QueryModel.parseExprToDBObject( hintValue );
        }
        catch( OdaException ex )
        {
            // log and ignore
            DriverUtil.getLogger().log( Level.INFO, 
                    Messages.bind( "Unable to parse the Index Hint expression: {0}", hintValue ), ex ); //$NON-NLS-1$
        }
        return null;
    }

    public void setNoTimeOut( Boolean hasNoTimeOut )
    {
        getPropertiesMap().put( NO_TIMEOUT_PROP, hasNoTimeOut );
    }

    public boolean hasNoTimeOut()
    {
        return getBooleanPropOrDefaultValue( NO_TIMEOUT_PROP );
    }

    public void setPartialResultsOk( Boolean isPartialResultsOk )
    {
        getPropertiesMap().put( PARTIAL_RESULTS_PROP, isPartialResultsOk );
    }

    public boolean isPartialResultsOk()
    {
        return getBooleanPropOrDefaultValue( PARTIAL_RESULTS_PROP );
    }

    public void setFindQueryExpr( String findQueryExpr )
    {
        getPropertiesMap().put( FIND_QUERY_EXPR_PROP, findQueryExpr );
    }

    public String getFindQueryExpr()
    {
        return getStringPropOrEmptyValue( FIND_QUERY_EXPR_PROP );
    }
    
    DBObject getFindQueryExprAsParsedObject() throws OdaException
    {
        String queryExprText = getFindQueryExpr();
        if( queryExprText.isEmpty() )
            return null;
        
        return QueryModel.parseExprToDBObject( queryExprText );
    }

    public void setSortExpr( String sortExpr )
    {
        getPropertiesMap().put( SORT_EXPR_PROP, sortExpr );
    }

    public String getSortExpr()
    {
        return getStringPropOrEmptyValue( SORT_EXPR_PROP );
    }
    
    DBObject getSortExprAsParsedObject() throws OdaException
    {
        String sortExprText = getSortExpr();
        if( sortExprText.isEmpty() )
            return null;
        
        return QueryModel.parseExprToDBObject( sortExprText );
    }

    // Utility methods
    
    private String getStringPropOrEmptyValue( String propName )
    {
        String value = getStringPropertyValue( getPropertiesMap(), propName );
        return value != null ? value : DriverUtil.EMPTY_STRING;        
    }
    
    private static String getStringPropertyValue( Map<String,Object> propertiesMap, String propertyName )
    {
        Object propValue = propertiesMap.get( propertyName );
        return propValue instanceof String ? ((String)propValue).trim() : null;

    }
    
    private Boolean getBooleanPropOrDefaultValue( String propName )
    {
        Boolean value = getBooleanPropertyValue( getPropertiesMap(), propName );
        if( value == null ) // no property value defined, get default value instead
            value = getBooleanPropertyValue( sm_defaultPropsMap, propName );
        return value;        
    }
    
    private static Boolean getBooleanPropertyValue( Map<String,Object> propertiesMap, String propertyName )
    {
        Object propValue = propertiesMap.get( propertyName );
        if( propValue instanceof Boolean )
            return (Boolean)propValue;
        if( propValue instanceof String && !((String)propValue).isEmpty() )
            return Boolean.valueOf( (String)propValue );
        return null;
    }
    
    private Integer getIntPropOrDefaultValue( String propName )
    {
        Integer value = getIntPropertyValue( getPropertiesMap(), propName );
        if( value == null ) // no property value defined, get default value instead
            value = getIntPropertyValue( sm_defaultPropsMap, propName );
        return value;        
    }

    private static Integer getIntPropertyValue( Map<String,Object> propertiesMap, String propertyName )
    {
        Object propValue = propertiesMap.get( propertyName );
        try
        {
            if( propValue instanceof Integer )
                return (Integer)propValue;
            if( propValue instanceof String && !((String)propValue).isEmpty() )
                return Integer.valueOf( (String)propValue );
        }
        catch( NumberFormatException ex )
        {
            // log and ignore
            DriverUtil.getLogger().log( Level.INFO, 
                    Messages.bind( "Invalid integer value ({0}) found in the {1} property.", propValue, propertyName ), ex ); //$NON-NLS-1$
        }
        return null;
    }

    private static boolean hasIntPropertyValue( Map<String,Object> propertiesMap, String propertyName )
    {
        return getIntPropertyValue( propertiesMap, propertyName ) != null;
    }

}
