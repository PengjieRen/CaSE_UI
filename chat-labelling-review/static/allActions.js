window.allActions = {
  user: [
    {
      'title': '显示(Reveal)',
      'name': 'Reveal',
      'tooltip': '主动提出、细化或者转换到一个新的意图。'
    },
    {
      'title': '修正(Revise)',
      'name': 'Revise',
      'tooltip': '在有表达错误或者意识到表达不清晰的情况下主动修正意图。'
    },
    {
      'title': '解释(Interpret)',
      'name': 'Interpret',
      'tooltip': '系统回复后，通过回答/回应系统的问题，被动修改或者细化意图。'
    },
    {
      'title': '反馈(Feedback)',
      'name': 'Feedback',
      'tooltip': '对系统的回复进行反馈，尤其是提供的答案，是否满意。',
      'children': [
        {
          'title': '优秀(Perfect)',
          'name': 'Perfect'
        },
        {
          'title': '良好(Good)',
          'name': 'Good'
        },
        {
          'title': '一般(Fair)',
          'name': 'Fair'
        },
        {
          'title': '差(Bad)',
          'name': 'Bad'
        }
      ]
    },
    {
      'title': '请求(Request)',
      'name': 'Request',
      'tooltip': '请求系统重新表述、给出特定类型的答案、推荐',
      'children': [
        {
          'title': '重新表述(Rephrase)',
          'name': 'Rephrase'
        },
        {
          'title': '提供摘要(Summary)',
          'name': 'Summary'
        },
        {
          'title': '提供链接(Link)',
          'name': 'Link'
        },
        {
          'title': '推荐(Recommend)',
          'name': 'Recommendation'
        }
      ]
    },
    {
      'title': '闲聊(Chitchat)',
      'name': 'Chitchat',
      'tooltip': '意图无关的内容。'
    }
  ],
  system: [
    {
      'title': '明晰(clarify)',
      'name': 'Clarify',
      'tooltip': '通过询问问题，帮助用户逐步细化意图。',
      'children': [
        {
          'title': '是非(Yes/no)',
          'name': 'Yes_no'
        },
        {
          'title': '选择(Choice)',
          'name': 'Choice'
        },
        {
          'title': '开放(Open)',
          'name': 'Open',
          'allowEmptySearch': true
        }
      ]
    },
    {
      'title': '推荐(recommend)',
      'name': 'Recommend',
      'tooltip': '推荐一些和用户当前意图相关但是又不是完全一致的内容。包括主动推荐和被动推荐(被要求推荐)。允许一定跨度内推荐，比如去掉某些细化相关的关键词。'
    },
    {
      'title': '回答(answer)',
      'name': 'Answer',
      'tooltip': '通过理解之前的对话及观察搜索栏中结果，系统完全或者大致知晓用户意图，以合适的形式提供用户答案。注意：除非用户要求或者只适合提供链接，否则应首选摘要。',
      'children': [
        {
          'title': '总结(Summary)',
          'name': 'Summary'
        },
        {
          'title': '链接(Link)',
          'name': 'Link'
        },
        {
          'title': '无答案(NoAnswer)',
          'name': 'NoAnswer',
          'allowEmptySearch': true
        }
      ]
    },
    {
      'title': '请求(request)',
      'name': 'request',
      'tooltip': '请求用户重新表述或者给予反馈',
      'children': [
        {
          'title': '重新表述(Rephrase)',
          'name': 'Rephrase'
        },
        {
          'title': '反馈(Feedback)',
          'name': 'Feedback'
        }
      ]
    },
    {
      'title': '闲聊(Chitchat)',
      'name': 'Chitchat',
      'allowEmptySearch': true,
      'tooltip': '与细化用户意图/返回结果无关的回复。鼓励大家发散思维，给出合理的回复.'
    }
  ]
}
