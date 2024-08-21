function numMulti(num1, num2) {
  let baseNum = 0
  try {
    baseNum += num1.toString().split('.')[1].length
  } catch (e) {}
  try {
    baseNum += num2.toString().split('.')[1].length
  } catch (e) {}
  return (Number(num1.toString().replace('.', '')) * Number(num2.toString().replace('.', ''))) / Math.pow(10, baseNum)
}
export { numMulti }

export function toDynamicTableData(input) {
  if (typeof input === 'undefined' || input === null) {
    return null
  }

  var dynamicTableData = {
    columns: [],
    columnsOrigin: [],
    data: []
  }

  dynamicTableData.data = Array(input.data.length)

  for (let i = 0; i < input.columns.length; i++) {
    dynamicTableData.columns.push({
      dataItem: 'col' + i,
      dataName: input.columns[i]
    })

    dynamicTableData.columnsOrigin.push({
      dataItem: 'col' + i,
      dataName: input.columns[i]
    })

    for (let j = 0; j < input.data.length; j++) {
      if (!dynamicTableData.data[j]) {
        dynamicTableData.data[j] = {}
      }

      dynamicTableData.data[j]['col' + i] = input.data[j][i]
    }
  }

  return dynamicTableData
}

export function handleParamsValid(params) {
  const validParams = {}
  Object.keys(params).forEach((key) => {
    if (!(params[key] === undefined || params[key] === null || params[key] === '')) {
      validParams[key] = params[key]
    }
  })
  return validParams
}
