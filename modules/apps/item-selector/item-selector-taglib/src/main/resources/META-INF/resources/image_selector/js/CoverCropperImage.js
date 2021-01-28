/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import PropTypes from 'prop-types';
import React from 'react';

const STR_VERTICAL = 'vertical';

class CoverCropperImage extends React.Component {
	static propTypes = {
		direction: PropTypes.string,
		imageSrc: PropTypes.string.isRequired,
		portletNamespace: PropTypes.string.isRequired,
	};

	static defaultProps = {
		direction: STR_VERTICAL,
	};

	constructor(props) {
		super(props);
		this.state = {
			dragging: false,
			position: {x: 0, y: 0},
			rel: null,
		};
	}

	componentDidUpdate(prevProps, prevState) {
		if (this.state.dragging && !prevState.dragging) {
			document.addEventListener('mousemove', this.onMouseMove.bind(this));
			document.addEventListener('mouseup', this.onMouseUp.bind(this));
		}
		else if (!this.state.dragging && prevState.dragging) {
			document.removeEventListener(
				'mousemove',
				this.onMouseMove.bind(this)
			);
			document.removeEventListener('mouseup', this.onMouseUp.bind(this));
		}
	}

	onMouseDown(event) {
		if (event.button !== 0) {
			return;
		}

		const pos = event.currentTarget.getBoundingClientRect();

		this.setState({
			dragging: true,
			rel: {
				x: event.pageX - pos.left,
				y: event.pageY - pos.top,
			},
		});

		event.stopPropagation();
		event.preventDefault();
	}

	onMouseMove(event) {
		if (!this.state.dragging) {
			return;
		}

		const {position, rel} = this.state;
		const vertical = this.props.direction === STR_VERTICAL;

		this.setState({
			position: {
				x: !vertical ? event.pageX - rel.x : position.x,
				y: vertical ? event.pageY - rel.y : position.y,
			},
		});

		event.stopPropagation();
		event.preventDefault();
	}

	onMouseUp(event) {
		this.setState({dragging: false});

		event.stopPropagation();
		event.preventDefault();
	}

	render() {
		const {position} = this.state;
		const {imageSrc, portletNamespace} = this.props;

		return (
			<div className="image-wrapper">
				<img
					alt={Liferay.Language.get('current-image')}
					className="current-image"
					id={`${portletNamespace}image`}
					onMouseDown={this.onMouseDown.bind(this)}
					src={imageSrc}
					style={{
						left: position.x,
						position: 'relative',
						top: position.y,
					}}
				/>
			</div>
		);
	}
}

export default CoverCropperImage;
