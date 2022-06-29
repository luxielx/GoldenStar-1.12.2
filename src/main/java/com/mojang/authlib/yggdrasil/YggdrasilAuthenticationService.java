// 
// Decompiled by Procyon v0.5.30
// 

package com.mojang.authlib.yggdrasil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.UUIDTypeAdapter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;

public class YggdrasilAuthenticationService extends HttpAuthenticationService {
    private final String clientToken;
    private final Gson gson;

    public YggdrasilAuthenticationService(final Proxy proxy, final String clientToken) {
        super(proxy);
        this.clientToken = clientToken;
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter((Type) GameProfile.class, (Object) new GameProfileSerializer());
        builder.registerTypeAdapter((Type) PropertyMap.class, (Object) new PropertyMap.Serializer());
        builder.registerTypeAdapter((Type) UUID.class, (Object) new UUIDTypeAdapter());
        builder.registerTypeAdapter((Type) ProfileSearchResultsResponse.class, (Object) new ProfileSearchResultsResponse.Serializer());
        this.gson = builder.create();
    }

    public UserAuthentication createUserAuthentication(final Agent agent) {
        return (UserAuthentication) new YggdrasilUserAuthentication(this, agent);
    }

    public MinecraftSessionService createMinecraftSessionService() {
        return (MinecraftSessionService) new YggdrasilMinecraftSessionService(this);
    }

    public GameProfileRepository createProfileRepository() {
        return (GameProfileRepository) new YggdrasilGameProfileRepository(this);
    }

    protected <T extends Response> T makeRequest(final URL url, final Object input, final Class<T> classOfT) throws AuthenticationException {
        try {
            final String jsonResult = (input == null) ? this.performGetRequest(url) : this.performPostRequest(url, this.gson.toJson(input), "application/json");
            final T result = (T) this.gson.fromJson(jsonResult, (Class) classOfT);
            if (result == null) {
                return null;
            }
            if (!StringUtils.isNotBlank((CharSequence) result.getError())) {
                return result;
            }
            if ("UserMigratedException".equals(result.getCause())) {
                throw new UserMigratedException(result.getErrorMessage());
            }
            if ("ForbiddenOperationException".equals(result.getError())) {
                throw new InvalidCredentialsException(result.getErrorMessage());
            }
            throw new AuthenticationException(result.getErrorMessage());
        } catch (IOException e) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", (Throwable) e);
        } catch (IllegalStateException e2) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", (Throwable) e2);
        } catch (JsonParseException e3) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", (Throwable) e3);
        }
    }

    public String getClientToken() {
        return this.clientToken;
    }

    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        public GameProfile deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = (JsonObject) json;
            final UUID id = object.has("id") ? ((UUID) context.deserialize(object.get("id"), (Type) UUID.class)) : null;
            final String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            return new GameProfile(id, name);
        }

        public JsonElement serialize(final GameProfile src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            if (src.getId() != null) {
                result.add("id", context.serialize((Object) src.getId()));
            }
            if (src.getName() != null) {
                result.addProperty("name", src.getName());
            }
            return (JsonElement) result;
        }
    }
}
