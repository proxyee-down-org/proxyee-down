package lee.study.down.model;

import io.netty.handler.codec.http.HttpHeaders;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public class HttpHeadsInfo extends HttpHeaders implements Serializable {

  private static final long serialVersionUID = 9051999778842339663L;
  private Map<String, String> map;

  public HttpHeadsInfo() {
    this.map = new HashMap<>();
  }

  @Override
  public String get(String name) {
    Optional<String> optional = map.keySet().stream().filter(k -> k.equalsIgnoreCase(name))
        .findAny();
    if (optional.isPresent()) {
      return map.get(optional.get());
    }
    return null;
  }

  @Override
  public Integer getInt(CharSequence name) {
    String value = get(name);
    return value == null ? null : Integer.valueOf(get(name));
  }

  @Override
  public int getInt(CharSequence name, int defaultValue) {
    Integer value = getInt(name);
    return value == null ? defaultValue : value;
  }

  @Override
  public Short getShort(CharSequence name) {
    String value = get(name);
    return value == null ? null : Short.valueOf(get(name));
  }

  @Override
  public short getShort(CharSequence name, short defaultValue) {
    Short value = getShort(name);
    return value == null ? defaultValue : value;
  }

  @Override
  public Long getTimeMillis(CharSequence name) {
    return null;
  }

  @Override
  public long getTimeMillis(CharSequence name, long defaultValue) {
    return 0;
  }

  @Override
  public List<String> getAll(String name) {
    String value = get(name);
    return value == null ? new ArrayList<>() : Arrays.asList(new String[]{value});
  }

  @Override
  public List<Entry<String, String>> entries() {
    return new ArrayList<>(map.entrySet());
  }

  @Override
  public boolean contains(String name) {
    return map.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(name));
  }

  @Deprecated
  @Override
  public Iterator<Entry<String, String>> iterator() {
    return map.entrySet().iterator();
  }

  @Override
  public Iterator<Entry<CharSequence, CharSequence>> iteratorCharSequence() {
    Map<CharSequence, CharSequence> temp = new HashMap<>();
    map.entrySet().forEach((entry) -> temp.put(entry.getKey(), entry.getValue()));
    return temp.entrySet().iterator();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Set<String> names() {
    return map.keySet();
  }

  @Override
  public HttpHeaders add(String name, Object value) {
    map.put(name, value.toString());
    return this;
  }

  @Override
  public HttpHeaders add(String name, Iterable<?> values) {
    return add(name, values);
  }

  @Override
  public HttpHeaders addInt(CharSequence name, int value) {
    return add(name, value);
  }

  @Override
  public HttpHeaders addShort(CharSequence name, short value) {
    return add(name, value);
  }

  @Override
  public HttpHeaders set(String name, Object value) {
    return add(name, value);
  }

  @Override
  public HttpHeaders set(String name, Iterable<?> values) {
    return add(name, values);
  }

  @Override
  public HttpHeaders setInt(CharSequence name, int value) {
    return add(name, value);
  }

  @Override
  public HttpHeaders setShort(CharSequence name, short value) {
    return add(name, value);
  }

  @Override
  public HttpHeaders remove(String name) {
    map.keySet().stream().filter(k -> k.equalsIgnoreCase(name)).findAny()
        .ifPresent(k -> map.remove(k));
    return this;
  }

  @Override
  public HttpHeaders clear() {
    map.clear();
    return this;
  }
}
