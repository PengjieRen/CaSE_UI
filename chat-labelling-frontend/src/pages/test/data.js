let data = {
  actionData: {
    'user': [
      {
        'title': 'Reveal',
        'name': 'Reveal',
        'tooltip': 'Start a new search conversation.'
      },
      {
        'title': 'Revise',
        'name': 'Revise',
        'tooltip': 'Make a correction.'
      },
      {
        'title': 'Shift',
        'name': 'Shift',
        'tooltip': 'Start a new search conversation by accepting recommendations.'
      },
      {
        'title': 'Interpret',
        'name': 'Interpret',
        'tooltip': 'Refine the current question proactively or by answering a clarifying question.'
      },
      {
        'title': 'Feedback',
        'name': 'Feedback',
        'tooltip': 'Reveal satisfaction to system responses.',
        'children': [
          {
            'title': 'Perfect',
            'name': 'Perfect'
          },
          {
            'title': 'Good',
            'name': 'Good'
          },
          {
            'title': 'Fair',
            'name': 'Fair'
          },
          {
            'title': 'Bad',
            'name': 'Bad'
          }
        ]
      },
      {
        'title': 'Request',
        'name': 'Request',
        'tooltip': 'Request system to give expected answer types.',
        'children': [
          {
            'title': 'Summary',
            'name': 'Summary'
          },
          {
            'title': 'Link',
            'name': 'Link'
          },
          {
            'title': 'Factoid',
            'name': 'Factoid'
          },
          {
            'title': 'Recommendation',
            'name': 'Recommendation'
          }
        ]
      },
      {
        'title': 'Chitchat',
        'name': 'Chitchat',
        'tooltip': 'Greeting, etc.',
        'allowEmptySearch': true
      },
      {
        'title': 'Not-clear',
        'name': 'Not-clear',
        'tooltip': 'Cannot understand system response.'
      }
    ],
    'system': [
      {
        'title': 'Clarify',
        'name': 'Clarify',
        'tooltip': 'Choose some suggested queries and ask users a clarifying question.',
        'children': [
          {
            'title': 'Yes',
            'name': 'Yes'
          },
          {
            'title': 'No',
            'name': 'No',
            'allowEmptySearch': true
          },
          {
            'title': 'Choice',
            'name': 'Choice'
          },
          {
            'title': 'Open',
            'name': 'Open'
          }
        ]
      },
      {
        'title': 'Recommend',
        'name': 'Recommend',
        'tooltip': 'Choose some related queries based on which to recommend related topics.'
      },
      {
        'title': 'Answer',
        'name': 'Answer',
        'tooltip': 'Choose some results and respond to users.',
        'children': [
          {
            'title': 'Summary',
            'name': 'Summary'
          },
          {
            'title': 'Link',
            'name': 'Link'
          },
          {
            'title': 'Factoid',
            'name': 'Factoid'
          }
        ]
      },
      {
        'title': 'Request',
        'name': 'Request',
        'tooltip': 'Can understand user response and request users to reformulate questions.'
      },
      {
        'title': 'Chitchat',
        'name': 'Chitchat',
        'tooltip': 'Greeting, etc.',
        'allowEmptySearch': true
      },
      {
        'title': 'Not-clear',
        'name': 'Not-clear',
        'tooltip': 'Cannot understand system response.'
      }
    ]
  },
  history: [
    {
      message: 'hello，how are you？',
      time: '2020/02/12 16:00',
      type: 'user'
    },
    {
      message: 'fine, thank you and you?',
      time: '2020/02/12 16:02',
      type: 'sys'
    },
    {
      message: 'i am fine too',
      time: '2020/02/12 16:04',
      type: 'user'
    }
  ],
  searchResults: {}
}
for (let index in data.actionData.system) {
  let action = data.actionData.system[index]
  if (['Request', 'Chitchat', 'Not-clear'].indexOf(action.name) >= 0) { continue }
  data.searchResults[action.name] = buildSearchResult(action.name)
}
function buildSearchResult (name) {
  let arr = []
  for (let i = 0; i < (name === 'Reveal' ? 10 : 5); i++) {
    arr.push({
      id: name + '-' + i,
      title: 'title-' + name + '-' + i,
      content: name === 'Reveal' ? 'This is content ' + name + ' ' + i + '--' + new Date() : ''
    })
  }
  return arr
}

export default data
