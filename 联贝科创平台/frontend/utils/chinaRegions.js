import regionMap from './chinaRegions.json'

/** 中国省级行政区（与 chinaRegions.json 保持一致） */
export const CHINA_PROVINCES = Object.keys(regionMap)

/** 获取某省下的城市列表 */
export function getCitiesByProvince(province) {
  return regionMap[province] ? [...regionMap[province]] : []
}

/** 构建 u-picker 双列初始数据 */
export function buildProvinceCityColumns(province, city) {
  const provinces = CHINA_PROVINCES
  const targetProvince = province && regionMap[province] ? province : provinces[0]
  const cities = getCitiesByProvince(targetProvince)
  return [provinces, cities]
}

/** 计算 u-picker 默认索引 */
export function getProvinceCityIndex(province, city) {
  const provinces = CHINA_PROVINCES
  const provinceIdx = Math.max(0, provinces.indexOf(province))
  const cities = getCitiesByProvince(provinces[provinceIdx])
  const cityIdx = city ? Math.max(0, cities.indexOf(city)) : 0
  return [provinceIdx, cityIdx]
}

/** 校验省、市是否在字典内 */
export function isValidProvinceCity(province, city) {
  const cities = regionMap[province]
  return Array.isArray(cities) && cities.includes(city)
}

export default regionMap
