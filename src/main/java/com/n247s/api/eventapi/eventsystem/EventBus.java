package com.n247s.api.eventapi.eventsystem;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.n247s.api.eventapi.EventApi;

public class EventBus
{
	private static final Logger log = EventApi.logger;
	protected final HashMap<Class<? extends EventType>, CallHandler> EventList = new HashMap<Class<? extends EventType>, CallHandler>();
	
	/**
	 * Be very careful when creating your own EventBus! Only create one when you really need to, since this is the most sensitive part of the CustomEventSystem,
	 * messing up the system is rather easy achievable.
	 */
	public EventBus(){}
	
	/**
	 * @param eventType
	 * @return - true if EventType is Canceled.
	 */
	public boolean raiseEvent(Class<? extends EventType> eventType)
	{
		if(!EventList.containsKey(eventType))
			EventList.put(eventType, new EventApiCallHandler(eventType));
		return ((CallHandler)EventList.get(eventType)).CallInstances();
	}
	
	/**
	 * used to bind a custom CallHandler to a EventType.
	 * @param eventType
	 * @param callHandler
	 */
	public void bindCallHandler(CallHandler callHandler)
	{
		if(EventList.containsKey(callHandler.eventType))
			EventList.remove(callHandler.eventType);
		EventList.put(callHandler.eventType, callHandler);
	}
	
	/**
	 * With this method you can add an EventListner(Class object/Class instance) which should be Called on eventRaise.
	 * 
	 * @param listner
	 * @throws IllegalArgumentException - If a CustomEventSubscribed Method contains more than one parameter,
	 * 		or if the parameter is not an instance of EventType.class.
	 */
	public void RegisterEventListner(Object listner)
	{
		Class clazz;
		if(!(listner instanceof Class))
			clazz = listner.getClass();
		else clazz = (Class) listner;
		
		Method[] methods = clazz.getMethods();
		
		for (int i = 0; i < methods.length; i++)
		{
			Method currentMethod = methods[i];
			if (currentMethod.isAnnotationPresent(CustomEventSubscribe.class))
			{
				if (currentMethod.getParameterTypes().length > 1)
					log.catching(new IllegalArgumentException("An CustomEventSubScribed Method Can't have more than one Parameter!"));
				if(!(EventType.class.isAssignableFrom(currentMethod.getParameterTypes()[0])))
					log.catching(new IllegalArgumentException("The Parameter isn't an EventType!"));
				
				CustomEventSubscribe annotation = currentMethod.getAnnotation(CustomEventSubscribe.class);
				Class<? extends EventType> methodEventType = (Class<? extends EventType>) currentMethod.getParameterTypes()[0];
				
				getCallHandlerFromEventType(methodEventType).RegisterEventListner(annotation.eventPriority(), listner, currentMethod);
			}
		}
	}
	
	public void RegisterEventListners(List<Object> listnersList)
	{
		for(int i = 0; i < listnersList.size(); i++)
		{
			RegisterEventListner(listnersList.get(i));
		}
	}
	
	/**
	 * With this method you can remove an EventListner(Class object/Class instance).
	 * 
	 * @param Listner
	 * @throws NullPointerException - if the EventListner isn't registered.
	 */
	public void removeEventListner(Object Listner)
	{
		Class clazz;
		if(!(Listner instanceof Class))
			clazz = Listner.getClass();
		else clazz = (Class)Listner;
		
		Method[] methodArray = clazz.getMethods();
		
		for(int i = 0; i < methodArray.length; i++)
		{
			Method currentMethod = methodArray[i];
			
			if(currentMethod.isAnnotationPresent(CustomEventSubscribe.class))
			{
				if (currentMethod.getParameterTypes().length > 1)
					log.catching(new IllegalArgumentException("An CustomEventSubScribed Method Can't have more than one Parameter!"));
				if (Listner instanceof Class && !Modifier.isStatic(currentMethod.getModifiers()))
					log.catching(new IllegalArgumentException("An CustomEventSubScribed Method Can't be non-static if you register an Class Object!"));
				if(!(EventType.class.isAssignableFrom(currentMethod.getParameterTypes()[0])))
					log.catching(new IllegalArgumentException("The Parameter of a CustomEventSubscribed method isn't an EventType!"));
				
				CustomEventSubscribe annotation = currentMethod.getAnnotation(CustomEventSubscribe.class);
				Class<? extends EventType> methodEventType = (Class<? extends EventType>) currentMethod.getParameterTypes()[0];
				
				getCallHandlerFromEventType(methodEventType).removeListner(annotation.eventPriority(), Listner);
			}
		}
	}
	
	public void removeEventListners(List<Object> listnersList)
	{
		for(int i = 0; i < listnersList.size(); i++)
		{
			removeEventListner(listnersList.get(i));
		}
	}
	
	/**
	 * @param eventTypeClass
	 * @return The instance of the CallHandler of this specific EventType.
	 * 		Note that if no CallHanlder is assigned, a default instance is returned!
	 */
	public CallHandler getCallHandlerFromEventType(Class<? extends EventType> eventTypeClass)
	{
		if(!EventList.containsKey(eventTypeClass))
			EventList.put(eventTypeClass, new EventApiCallHandler(eventTypeClass));
		return EventList.get(eventTypeClass);
	}
}