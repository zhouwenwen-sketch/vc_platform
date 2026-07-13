package com.lianbei.vc.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/** 中国省市区字典（与 frontend/utils/chinaRegions.json 保持一致） */
public final class ChinaRegions {

  private static volatile Map<String, List<String>> regionMap;

  private ChinaRegions() {}

  public static List<String> all() {
    return new ArrayList<>(regionData().keySet());
  }

  public static List<String> citiesOf(String province) {
    List<String> cities = regionData().get(province);
    return cities == null ? List.of() : List.copyOf(cities);
  }

  public static boolean isValidProvinceCity(String province, String city) {
    if (province == null || province.isBlank() || city == null || city.isBlank()) {
      return false;
    }
    List<String> cities = regionData().get(province.trim());
    return cities != null && cities.contains(city.trim());
  }

  public static boolean isChina(String location) {
    if (location == null || location.isBlank()) {
      return false;
    }
    String loc = location.trim();
    if (regionData().containsKey(loc)) {
      return true;
    }
    if ("深圳市".equals(loc)) {
      return true;
    }
    return loc.endsWith("省") || loc.endsWith("市");
  }

  private static Map<String, List<String>> regionData() {
    if (regionMap != null) {
      return regionMap;
    }
    synchronized (ChinaRegions.class) {
      if (regionMap != null) {
        return regionMap;
      }
      try {
        ClassPathResource resource = new ClassPathResource("config/china-regions.json");
        ObjectMapper mapper = new ObjectMapper();
        regionMap =
            mapper.readValue(
                resource.getInputStream(), new TypeReference<LinkedHashMap<String, List<String>>>() {});
      } catch (IOException e) {
        regionMap = Map.of();
      }
      return regionMap;
    }
  }
}
