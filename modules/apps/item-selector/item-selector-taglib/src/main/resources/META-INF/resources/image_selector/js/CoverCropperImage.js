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

const STR_BOTH = 'both';
const STR_HORIZONTAL = 'horizontal';
const STR_VERTICAL = 'vertical';

class CoverCropperImage extends React.Component {
	static propTypes = {
		direction: PropTypes.string,
		handleImageUpdate: PropTypes.func,
		imageSrc: PropTypes.string.isRequired,
		portletNamespace: PropTypes.string.isRequired,
	};

	static defaultProps = {
		direction: STR_VERTICAL,
		handleImageUpdate: () => {}
	};

	constructor(props) {
		super(props);
		this.state = {
			dragging: false,
			position: {x: 0, y: 0},
			rel: null,
		};

		this.containerRef = React.createRef();
		this.imageRef = React.createRef();
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

		const imageContainer = this.containerRef.current;
		const pos = imageContainer.getBoundingClientRect();

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

		const imageContainer = this.containerRef.current;
		const image = this.imageRef.current;

		const pos = imageContainer.getBoundingClientRect();

		let horizontalPos = position.x;
		let verticalPos = position.y;

		if (this.props.direction === STR_HORIZONTAL || direction === STR_BOTH) {
			const horizontalDiff = event.pageX - pos.left - rel.x;
			horizontalPos = horizontalPos +  horizontalDiff;

			if (horizontalPos >= 0 || horizontalPos < imageContainer.offsetWidth - image.offsetWidth) {
				event.preventDefault();
				return;
			}
		}

		if (this.props.direction === STR_VERTICAL || direction === STR_BOTH) {
			const verticalDiff = event.pageY - pos.top - rel.y;
			verticalPos = verticalPos + verticalDiff;

			if (verticalPos >= 0 || verticalPos < imageContainer.offsetHeight - image.offsetHeight) {
				event.preventDefault();
				return;
			}
		}

		this.setState({
			position: {
				x: horizontalPos,
				y: verticalPos,
			},
		});

		event.stopPropagation();
		event.preventDefault();
	}

	onMouseUp(event) {
		this.setState({dragging: false});

		event.stopPropagation();
		event.preventDefault();

		const cropRegion = Liferay.Util.getCropRegion(this.imageRef.current, {
			height: this.containerRef.current.offsetHeight,
			x: Math.abs(this.state.position.x),
			y: Math.abs(this.state.position.y),
		});

		this.props.handleImageUpdate(JSON.stringify(cropRegion));
	}

	render() {
		const {position} = this.state;
		const {imageSrc, portletNamespace} = this.props;

		return (
			<div className="cropper image-wrapper" ref={this.containerRef}>
				<img
					alt={Liferay.Language.get('current-image')}
					className="current-image"
					id={`${portletNamespace}image`}
					onMouseDown={this.onMouseDown.bind(this)}
					ref={this.imageRef}
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
