/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {RefObject, useContext, useEffect} from 'react';
import {DragSourceMonitor, useDrag} from 'react-dnd';

import DnDContext from '../DnDContext';

export const FDS_ITEM_DRAG_TYPE = 'FDS_ITEM';

const useFDSDrag = ({
	dragSourceRef,
	item,
}: {
	dragSourceRef: RefObject<HTMLElement>;
	item: any;
}) => {
	const {fileDropSettings} = useContext(DnDContext);

	const canDrag = Boolean(fileDropSettings?.onItemDrop);

	const [{isDragging}, dragRef] = useDrag({
		canDrag: () => canDrag,
		collect: (monitor: DragSourceMonitor) => ({
			isDragging: monitor.isDragging(),
		}),
		item: {...item, type: FDS_ITEM_DRAG_TYPE},
	});

	useEffect(() => {
		if (canDrag && dragSourceRef?.current) {
			dragRef(dragSourceRef);
		}
	}, [canDrag, dragRef, dragSourceRef]);

	return {canDrag, isDragging};
};

export default useFDSDrag;
