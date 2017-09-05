package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.vdurmont.emoji.Emoji;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IEmoji;

/**
 * Marks the annotated method as a deserializer.
 * <p>
 * A deserializer should:
 * <ul>
 * <li>Be a public method.</li>
 * <li>Return a <code>String</code>, {@link EmbedObject}, {@link Emoji}, or
 * {@link IEmoji}</li>
 * <li>Take an object type that you wish to return from methods as its sole
 * parameter.</li>
 * </ul>
 * 
 * @author Nixill
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Serializer {}
