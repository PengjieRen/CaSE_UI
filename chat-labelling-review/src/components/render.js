export default {
  name: 'Render',
  functional: true,
  props: {
    render: Function,
    data: Object
  },
  render: (h, ctx) => {
    return ctx.props.render(h, ctx.props.data)
  }
}
